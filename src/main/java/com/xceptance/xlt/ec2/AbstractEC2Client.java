/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.ec2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeRegionsRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.util.Base64;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.engine.TimeoutException;

/**
 * The {@link AbstractEC2Client} class is the entry point to the simple front-end for managing AWS EC2 instances.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
abstract public class AbstractEC2Client
{
    /**
     * The default region to use when requesting a client.
     */
    private static final String DEFAULT_REGION = "us-east-1";

    /**
     * Internal cache used when requesting a client for a given region.
     */
    private final HashMap<String, AmazonEC2> clientsByRegion = new HashMap<>();

    private final ClientConfiguration clientConfig;

    /**
     * The AWS configuration.
     */
    protected final AwsConfiguration awsConfiguration;

    /**
     * The "running" state constant. (Instance state)
     */
    public static final String STATE_RUNNING = "running";

    /**
     * The "available" state constant. (Image state)
     */
    public static final String STATE_AVAILABLE = "available";

    private static final long INSTANCE_STATE_POLLING_INTERVAL = 1000;

    private static final long IMAGE_STATE_POLLING_INTERVAL = 1000;

    /**
     * The log facility.
     */
    protected static final Log log = LogFactory.getLog(AbstractEC2Client.class);

    private static final Pattern INSTANCE_PRICING_JSON_PATTERN = Pattern.compile("callback\\(\\{vers:[\\d.]+,config:\\{.+?,regions:\\[.+\\]\\}\\}\\);");

    public AbstractEC2Client() throws Exception
    {
        this(null, null);
    }

    public AbstractEC2Client(final String accessKey, final String secretKey) throws Exception
    {
        // create the AWS EC2 client
        try
        {
            awsConfiguration = new AwsConfiguration(accessKey, secretKey);

            clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(awsConfiguration.getProtocol());
            clientConfig.setProxyHost(awsConfiguration.getProxyHost());
            clientConfig.setProxyPort(awsConfiguration.getProxyPort());
            clientConfig.setProxyUsername(awsConfiguration.getProxyUserName());
            clientConfig.setProxyPassword(awsConfiguration.getProxyPassword());

        }
        catch (final Exception e)
        {
            System.err.println("Failed to initialize AWS EC2 client: " + e.getMessage());
            log.error("Failed to initialize AWS EC2 client", e);
            throw e;
        }
    }

    protected AmazonEC2 getClient(final Region region)
    {
        return clientForRegion(region == null ? DEFAULT_REGION : region.getRegionName());
    }

    private AmazonEC2 clientForRegion(final String regionName)
    {
        AmazonEC2 client = clientsByRegion.get(regionName);
        if (client == null)
        {
            client = AmazonEC2Client.builder().withClientConfiguration(clientConfig)
                                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsConfiguration.getAccessKey(),
                                                                                                              awsConfiguration.getSecretKey())))
                                    .withRegion(regionName).build();
            clientsByRegion.put(regionName, client);
        }

        return client;
    }

    /**
     * Waits until given instance has wanted state.
     *
     * @param region
     *            region of instance
     * @param instance
     *            instance to check
     * @param state
     *            wanted instance state
     * @param timeout
     *            how long do you want to wait for instance state
     * @return updated instance instance with wanted state
     * @throws Exception
     *             if instance doesn't have wanted state within given timeout.
     */
    protected Instance waitForInstanceState(final Region region, final Instance instance, final String state, final long timeout)
        throws Exception
    {
        final long deadline = System.currentTimeMillis() + timeout;
        final String instanceId = instance.getInstanceId();
        while (System.currentTimeMillis() < deadline)
        {
            final Instance foundInstance = getInstance(region, instanceId, new ArrayList<TagDescription>(), state);

            // match
            if (foundInstance != null)
            {
                return foundInstance;
            }
            else
            {
                // wait some time
                try
                {
                    Thread.sleep(INSTANCE_STATE_POLLING_INTERVAL);
                }
                catch (final Exception e)
                {
                    // ignore
                }
            }
        }

        throw new Exception("Instance didn't achieve state '" + state + "' within " + (timeout / 1000) + "s. Current state is '" +
                            instance.getState().getName() + "'");
    }

    /**
     * Looks up the specified image in given region and waits until it has the specified state.
     *
     * @param region
     *            region to lookup the image in
     * @param imageID
     *            the image to lookup
     * @param state
     *            the state to wait for
     * @param timeout
     *            time within the image must get the specified state
     * @return the current image object with wanted state
     * @throws Exception
     *             if the image didn't achieve the state within given time OR image wasn't found
     */
    protected Image waitForImageState(final Region region, final String imageID, final String state, final long timeout) throws Exception
    {
        Image image = null;

        final long deadline = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < deadline)
        {
            image = getImage(region, imageID);

            // check sate
            if (image != null && image.getState().equals(state))
            {
                return image;
            }
            else
            {
                // wait some time
                try
                {
                    Thread.sleep(IMAGE_STATE_POLLING_INTERVAL);
                }
                catch (final Exception e)
                {
                    // ignore
                }
            }
        }

        if (image == null)
        {
            throw new Exception("Image '" + imageID + "' not found in region '" + region.getRegionName() + "'.");
        }
        else
        {
            throw new Exception("Image didn't achieve state '" + state + "' within " + (timeout / 1000) + "s. Current state is '" +
                                image.getState() + "'");
        }
    }

    /**
     * Returns the list of availability zones in the specified region.
     *
     * @param region
     *            the region
     * @return the availability zones
     */
    protected List<AvailabilityZone> getAvailabilityZones(final Region region)
    {
        return getClient(region).describeAvailabilityZones().getAvailabilityZones();
    }

    /**
     * Get security group IDs for given region
     *
     * @param region
     *            the region
     * @return security group IDs for given region
     */
    protected List<SecurityGroup> getSecurityGroupIDs(final Region region)
    {
        final List<SecurityGroup> secGroups = getClient(region).describeSecurityGroups().getSecurityGroups();

        // sort the images by image description
        Collections.sort(secGroups, new Comparator<SecurityGroup>()
        {
            @Override
            public int compare(final SecurityGroup i1, final SecurityGroup i2)
            {
                final String d1 = StringUtils.defaultString(i1.getGroupName().toLowerCase());
                final String d2 = StringUtils.defaultString(i2.getGroupName().toLowerCase());
                return d1.compareTo(d2);
            }
        });

        return secGroups;
    }

    /**
     * Returns the list of machine images, which are available to the AWS user in the specified region. This list
     * includes the AMIs owned by the user as well as public AMIs provided by Xceptance.
     *
     * @param region
     *            the region
     * @param imageIds
     *            one or more AMI IDs
     * @return the images
     */
    protected List<Image> getImages(final Region region, final String... imageIds)
    {
        final DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withOwners("self", "614612213257")
                                                                                       .withImageIds(imageIds);
        final List<Image> images = getClient(region).describeImages(describeImagesRequest).getImages();

        return images;
    }

    /**
     * Get available key pairs for given region.
     *
     * @param region
     *            the region
     * @return list of key-pair available in given region
     */
    protected List<KeyPairInfo> getKeyPairs(final Region region)
    {
        final DescribeKeyPairsResult describeKeyPairsResult = getClient(region).describeKeyPairs();
        final List<KeyPairInfo> keyPairInfos = describeKeyPairsResult.getKeyPairs();

        // sort the images by image description
        Collections.sort(keyPairInfos, new Comparator<KeyPairInfo>()
        {
            @Override
            public int compare(final KeyPairInfo i1, final KeyPairInfo i2)
            {
                final String d1 = StringUtils.defaultString(i1.getKeyName());
                final String d2 = StringUtils.defaultString(i2.getKeyName());
                return d1.compareTo(d2);
            }
        });

        return keyPairInfos;
    }

    /**
     * Get specified image in given region.
     *
     * @param region
     *            region of image
     * @param imageId
     *            image ID to lookup
     * @return image with specified ID or <code>null</code> if no such image was found
     */
    protected Image getImage(final Region region, final String imageId)
    {
        final List<Image> images = getImages(region, imageId);
        Image image = null;
        for (final Image img : images)
        {
            if (img.getImageId().equals(imageId))
            {
                image = img;
                break;
            }
        }
        return image;
    }

    /**
     * Returns the IDs of the instances currently running in the specified region.
     *
     * @param region
     *            the region
     * @param tags
     *            tags to filter by
     * @return the instance IDs
     */
    protected List<String> getRunningInstanceIds(final Region region, final List<TagDescription> tags)
    {
        final ArrayList<String> instanceIds = new ArrayList<String>();

        for (final Instance instance : getInstances(region, tags, STATE_RUNNING))
        {
            instanceIds.add(instance.getInstanceId());
        }

        return instanceIds;
    }

    /**
     * Lookup specific instance in region.
     *
     * @param region
     *            region of instance
     * @param instanceID
     *            ID of instance
     * @param tags
     *            instance tags
     * @param state
     *            the instance state we want (may be <code>null</code>)
     * @return Found instance or <code>null</code> if no such instance was found
     */
    protected Instance getInstance(final Region region, final String instanceID, final List<TagDescription> tags, final String state)
    {
        // lookup instance
        final List<Instance> instances = getInstances(region, new ArrayList<TagDescription>(), state);
        for (final Instance instance : instances)
        {
            if (instance.getInstanceId().equals(instanceID))
            {
                return instance;
            }
        }

        return null;
    }

    /**
     * Returns the instances currently running in the specified region.
     *
     * @param region
     *            the region
     * @param tags
     *            the tags to match
     * @param state
     *            the instance state we want (may be <code>null</code>)
     * @return the instances
     */
    protected List<Instance> getInstances(final Region region, final List<TagDescription> tags, final String state)
    {
        // build the tag filter list
        final Map<String, Filter> filters = new HashMap<String, Filter>();

        for (final TagDescription tag : tags)
        {
            final String key = tag.getKey();
            Filter filter = filters.get(key);
            if (filter == null)
            {
                filter = new Filter("tag:" + key);
                filters.put(key, filter);
            }
            filter.withValues(tag.getValue());
        }

        if (state != null)
        {
            final Filter stateFilter = new Filter("instance-state-name");
            stateFilter.withValues(state);
            filters.put("instance-state", stateFilter);
        }

        final List<Instance> instances = new ArrayList<Instance>();

        final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.setFilters(filters.values());

        final DescribeInstancesResult describeInstancesResult = getClient(region).describeInstances(describeInstancesRequest);
        for (final Reservation reservation : describeInstancesResult.getReservations())
        {
            instances.addAll(reservation.getInstances());
        }

        return instances;
    }

    /**
     * Returns the instances currently running in the specified region.
     *
     * @param region
     *            the region
     * @param tags
     *            the tags to match
     * @return the instances
     */
    protected List<Instance> getInstances(final Region region, final List<TagDescription> tags)
    {
        return getInstances(region, tags, null);
    }

    /**
     * Returns the regions specified by the given region names. If no region name is given, all available regions will
     * be returned.
     *
     * @param regionNames
     *            the names of the regions in question
     * @return the regions
     */
    protected List<Region> getRegions(final String... regionNames)
    {
        final DescribeRegionsRequest describeRegionsRequest = new DescribeRegionsRequest();
        describeRegionsRequest.setRegionNames(Arrays.asList(regionNames));
        describeRegionsRequest.setAllRegions(true);

        final DescribeRegionsResult describeRegionsResult = getClient(null).describeRegions(describeRegionsRequest);
        final List<Region> regions = describeRegionsResult.getRegions();

        // sort the regions by region name
        Collections.sort(regions, (r1, r2) -> {
            final String s1 = StringUtils.defaultString(r1.getRegionName());
            final String s2 = StringUtils.defaultString(r2.getRegionName());
            return s1.compareTo(s2);
        });

        return regions;
    }

    /**
     * Get region with specified name.
     *
     * @param regionName
     *            name of region
     * @return region with specified name or <code>null</code> if there is not exactly 1 region with given name.
     */
    protected Region getRegion(final String regionName)
    {
        final List<Region> regions = getRegions(regionName);
        return regions.size() == 1 ? regions.get(0) : null;
    }

    /**
     * Parses the command-line arguments and returns a command-line object.
     *
     * @param options
     *            the command-line options
     * @param args
     *            the command-line arguments
     * @return the parsed command line
     */
    protected static CommandLine parseCommandLine(final Options options, final String[] args)
    {
        final CommandLineParser parser = new DefaultParser();

        try
        {
            return parser.parse(options, args);
        }
        catch (final ParseException ex)
        {
            printUsageInfo(options);
            System.exit(ProcessExitCodes.PARAMETER_ERROR);

            // will never get here
            return null;
        }
    }

    /**
     * Prints usage information according to the given command-line options.
     *
     * @param options
     *            the command-line options
     */
    protected static void printUsageInfo(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();

        System.out.println("Simple front-end application to manage AWS EC2 instances.");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("    ec2_admin [<options>]");
        System.out.println("      -> Run in interactive mode.\n");
        System.out.println("    ec2_admin run <region> <ami> <type> <count> <nameTag> [<options>]");
        System.out.println("      -> Start instances non-interactively.\n");
        System.out.println("    ec2_admin terminate <region> <nameTag>");
        System.out.println("      -> Terminate instances non-interactively.");

        formatter.setSyntaxPrefix("");
        formatter.setWidth(79);

        formatter.printHelp(" ", "Options:", options, "");
        System.out.println();
    }

    /**
     * Retrieves all subnets for the given region that belong to the given availability zone and/or VPC if given.
     * 
     * @param region
     *            the region
     * @param availabilityZone
     *            the availability zone (may be {@code null})
     * @param vpc
     *            the VPC (may be {@code null})
     * @return list of all subnets for the given region (and AZ/VPC if given)
     * @throws Exception
     */
    protected List<Subnet> getSubnets(final Region region, final AvailabilityZone availabilityZone, final Vpc vpc)
    {
        final DescribeSubnetsRequest req = new DescribeSubnetsRequest();

        final List<Filter> filters = new ArrayList<>();
        if (availabilityZone != null)
        {
            filters.add(new Filter("availability-zone", Arrays.asList(availabilityZone.getZoneName())));
        }
        if (vpc != null)
        {
            filters.add(new Filter("vpc-id", Arrays.asList(vpc.getVpcId())));
        }

        req.setFilters(filters);

        return getClient(region).describeSubnets(req).getSubnets();
    }

    /**
     * Retrieves the list of all VPCs defined for the given region.
     * 
     * @param region
     *            the region
     * @param vpcIds
     *            list of VPC-IDs (may be {@code null}) to use as filter
     * @return list of VPCs defined for given region
     */
    protected List<Vpc> getVpcs(final Region region, final List<String> vpcIds)
    {
        final DescribeVpcsRequest vpcReq = new DescribeVpcsRequest();
        if (vpcIds != null && !vpcIds.isEmpty())
        {
            vpcReq.withFilters(new Filter("vpc-id", vpcIds));
        }
        return getClient(region).describeVpcs(vpcReq).getVpcs();
    }

    /**
     * Starts new EC2 instances.
     *
     * @param region
     *            the target region
     * @param subnet
     *            the subnet to launch instance(s) into (pass {@code null} to pick any subnet marked as 'Default for
     *            AZ')
     * @param image
     *            the image to create the instance from
     * @param instanceType
     *            the instance type to use
     * @param count
     *            the number of instances to start
     * @param securityGroupIds
     *            list of security groups to apply for the instance
     * @param keyPairName
     *            the name of key pair to set up for the instance. Set to <code>null</code> if no key should be set up.
     * @param userData
     *            user data to pass to the instance. Might be null or empty if nothing should to be set.
     * @return list of run instances
     * @throws Exception
     *             in case of instance provider errors
     */
    protected List<Instance> runInstances(final Region region, Subnet subnet, final Image image, final String instanceType, final int count,
                                          final Collection<String> securityGroupIds, final String keyPairName, final String userData)
        throws Exception
    {
        final RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        // check if specific subnet is desired
        if (subnet == null)
        {
            // get all subnets of given region
            final List<Subnet> subnets = getSubnets(region, null, null);
            if (!subnets.isEmpty())
            {
                // get the default subnet or (if not present) take the first one
                subnet = subnets.parallelStream().filter(s -> s.isDefaultForAz()).findAny().orElse(subnets.get(0));
            }
        }

        // must have any subnet now
        if (subnet == null)
        {
            throw new Exception("No subnet available to launch instances into");
        }

        runInstancesRequest.setPlacement(new Placement(subnet.getAvailabilityZone()));
        runInstancesRequest.setSubnetId(subnet.getSubnetId());

        runInstancesRequest.setImageId(image.getImageId());
        runInstancesRequest.setInstanceType(instanceType);
        runInstancesRequest.setMinCount(count);
        runInstancesRequest.setMaxCount(count);

        if (StringUtils.isNotBlank(userData))
        {
            runInstancesRequest.setUserData(Base64.encodeAsString(userData.getBytes()));
        }

        if (securityGroupIds != null)
        {
            runInstancesRequest.setSecurityGroupIds(securityGroupIds);
        }

        if (StringUtils.isNotBlank(keyPairName))
        {
            runInstancesRequest.setKeyName(keyPairName);
        }

        final RunInstancesResult result = getClient(region).runInstances(runInstancesRequest);
        final List<Instance> instances = result.getReservation().getInstances();

        return instances;
    }

    /**
     * Terminates all running/stopped instances in the specified regions.
     *
     * @param regions
     *            the regions
     */
    protected void terminateInstances(final List<Region> regions, final List<TagDescription> tags)
    {
        System.out.println();

        for (final Region region : regions)
        {
            try
            {
                System.out.printf("Terminating %s instances in region '%s' ... ", (tags.isEmpty() ? "all" : "the selected"),
                                  region.getRegionName());

                final TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
                terminateInstancesRequest.setInstanceIds(getRunningInstanceIds(region, tags));
                getClient(region).terminateInstances(terminateInstancesRequest);

                System.out.println("OK.");
            }
            catch (final AmazonClientException e)
            {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }

    /**
     * Terminates running/stopped instance in the specified region.
     *
     * @param region
     *            the region
     * @param instance
     *            the instance
     */
    protected void terminateInstance(final Region region, final Instance instance)
    {
        final TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        terminateInstancesRequest.setInstanceIds(Arrays.asList(instance.getInstanceId()));

        getClient(region).terminateInstances(terminateInstancesRequest);
    }

    /**
     * Set a name tag to the given IDs (might be an instance or an image for example).
     *
     * @param ids
     *            IDs to set the name for
     * @param name
     *            name to set
     */
    protected void setNameTag(final Region region, final List<String> ids, final String name)
    {
        final List<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag().withKey("Name").withValue(name));

        // build tag request
        final CreateTagsRequest createTagRequest = new CreateTagsRequest();
        createTagRequest.setResources(ids);
        createTagRequest.setTags(tags);

        // set tag
        getClient(region).createTags(createTagRequest);
    }

    /**
     * Waits for the given instances to eventually exist. Before they exist they cannot be used or modified.
     * <p>
     * See <a href=
     * "http://docs.aws.amazon.com/AWSEC2/latest/APIReference/query-api-troubleshooting.html#eventual-consistency"
     * >here</a> for more information.
     *
     * @param instances
     *            the instances to wait for
     * @param timeout
     *            the time to wait
     * @throws InterruptedException
     *             when interrupted while waiting
     */
    protected void waitForInstancesToEventuallyExist(final Region region, final List<Instance> instances, final long timeout)
        throws InterruptedException
    {
        // collect instance IDs
        final List<String> instanceIds = getInstanceIds(instances);

        // waiting loop
        final long deadline = System.currentTimeMillis() + timeout;

        while (true)
        {
            AmazonServiceException lastException = null;

            try
            {
                // dummy request to describe the instances
                final DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                describeInstancesRequest.setInstanceIds(instanceIds);

                getClient(region).describeInstances(describeInstancesRequest);

                // no exception, return immediately
                return;
            }
            catch (final AmazonServiceException e)
            {
                // check if some instances were reported as non-existing
                if ("InvalidInstanceID.NotFound".equals(e.getErrorCode()))
                {
                    // yes, continue waiting, but remember the exception
                    lastException = e;
                    log.debug("Not all previously created instances are alive yet: " + e.getMessage());
                }
                else
                {
                    // no, must be other type of error, propagate it immediately
                    throw e;
                }
            }

            // check if there is still time left
            if (System.currentTimeMillis() < deadline)
            {
                // yes
                Thread.sleep(INSTANCE_STATE_POLLING_INTERVAL);
            }
            else
            {
                // no, leave the loop
                throw new TimeoutException("One or more instances did not become alive within " + (timeout / 1000) + " seconds",
                                           lastException);
            }
        }
    }

    /**
     * Gets the IDs of the given instances as a list.
     *
     * @param instances
     *            the list of instances
     * @return the corresponding list of IDs
     */
    protected List<String> getInstanceIds(final List<Instance> instances)
    {
        final List<String> instanceIds = new ArrayList<String>(instances.size());

        for (final Instance instance : instances)
        {
            instanceIds.add(instance.getInstanceId());
        }

        return instanceIds;
    }

    /**
     * Returns the instance pricing information as JSON plain string, either freshly downloaded from AWS or, in case the
     * download has failed, from the shipped sample file.
     *
     * @return instance pricing information as plain JSON string
     */
    protected String getPriceInfo()
    {
        String s = downloadPriceInfo(awsConfiguration.getInstancePricingUrl() + "?callback=callback&_=" + System.currentTimeMillis());
        if (s == null)
        {
            /*
             * Fall back to sample file shipped with XLT.
             */
            s = readSamplePriceInfo();
        }

        if (s == null)
        {
            return null;
        }

        return StringUtils.substringBeforeLast(StringUtils.substringAfter(s, "callback("), ")");
    }

    /**
     * Reads in the sample file that holds the instance pricing information.
     *
     * @return content of sample file on success, {@code null} otherwise
     */
    private String readSamplePriceInfo()
    {
        try (final InputStream is = getClass().getResourceAsStream("linux-od.min.js"))
        {
            if (is != null)
            {
                return IOUtils.toString(is, "UTF-8");
            }
        }
        catch (final Throwable t)
        {
            log.fatal("Failed to read sample price info file", t);
        }

        return null;
    }

    /**
     * Downloads the instance pricing information from the given URL and returns the response as string.
     *
     * @param urlString
     *            the download URL as string
     * @return content of downloaded response on success, {@code null} otherwise
     */
    private String downloadPriceInfo(final String uri)
    {
        final String errMsg = "Failed to download price info from URL '" + uri + "'";
        try
        {
            final HttpUriRequest request = new HttpGet(uri);
            final HttpResponse resp = getUnderlyingHttpClient().execute(request);

            final int statusCode = resp.getStatusLine().getStatusCode();
            if (200 != statusCode)
            {
                log.error(errMsg + ": server responded with status code " + statusCode);
            }
            else
            {
                final String responseContent = EntityUtils.toString(resp.getEntity());
                final Matcher m = INSTANCE_PRICING_JSON_PATTERN.matcher(responseContent);
                if (m.find())
                {
                    return responseContent;
                }
                log.error(errMsg + ": response is either no valid JSON or malformed.");
            }
        }
        catch (final Throwable t)
        {
            log.error(errMsg, t);
        }

        System.out.println("\nWARNING: Instance pricing could not be retrieved. Please check the log for details." +
                           "\n         Availability of instance types and their prices might be out of date.");

        return null;
    }

    /**
     * Returns the underlying HTTP client used by this' AWS client.
     *
     * @return HTTP client
     */
    protected HttpClient getUnderlyingHttpClient()
    {
        final AmazonEC2Client awsClient = (AmazonEC2Client) getClient(null);
        final AmazonHttpClient ahc = ReflectionUtils.readInstanceField(awsClient, "client");
        return ReflectionUtils.readInstanceField(ahc, "httpClient");
    }

    /**
     * Shuts down the EC2 clients that were potentially created by {@link #getClient(Region)}.
     * 
     * @see AmazonEC2#shutdown()
     */
    public void shutdown()
    {
        clientsByRegion.values().forEach(AmazonEC2::shutdown);
    }
}
