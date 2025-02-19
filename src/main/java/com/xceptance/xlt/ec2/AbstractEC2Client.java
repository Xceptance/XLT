/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.http.urlconnection.ProxyConfiguration;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AvailabilityZone;
import software.amazon.awssdk.services.ec2.model.CreateTagsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeRegionsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeRegionsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.ImageState;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.Placement;
import software.amazon.awssdk.services.ec2.model.Region;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagDescription;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.utils.BinaryUtils;
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
    private static final String DEFAULT_REGION = software.amazon.awssdk.regions.Region.US_EAST_1.id();

    /**
     * Internal cache used when requesting a client for a given region.
     */
    private final HashMap<String, Ec2Client> clientsByRegion = new HashMap<>();

    private final ProxyConfiguration proxyConfig;

    /**
     * The AWS configuration.
     */
    protected final AwsConfiguration awsConfiguration;

    private static final long INSTANCE_STATE_POLLING_INTERVAL = 1000;

    /**
     * The log facility.
     */
    protected static final Logger log = LoggerFactory.getLogger(AbstractEC2Client.class);

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

            if (awsConfiguration.getProxyHost() != null)
            {
                ProxyConfiguration.Builder proxyConfigBuilder = ProxyConfiguration.builder();
                proxyConfigBuilder.endpoint(new URIBuilder().setScheme(awsConfiguration.getProtocol())
                                                            .setHost(awsConfiguration.getProxyHost())
                                                            .setPort(awsConfiguration.getProxyPort()).build());
                proxyConfigBuilder.username(awsConfiguration.getProxyUserName());
                proxyConfigBuilder.password(awsConfiguration.getProxyPassword());
                proxyConfigBuilder.useSystemPropertyValues(Boolean.FALSE);
                proxyConfigBuilder.useEnvironmentVariablesValues(Boolean.FALSE);
                proxyConfig = proxyConfigBuilder.build();
            }
            else
            {
                proxyConfig = null;
            }

        }
        catch (final Exception e)
        {
            System.err.println("Failed to initialize AWS EC2 client: " + e.getMessage());
            log.error("Failed to initialize AWS EC2 client", e);
            throw e;
        }
    }

    protected Ec2Client getClient(final Region region)
    {
        return clientForRegion(region == null ? DEFAULT_REGION : region.regionName());
    }

    private Ec2Client clientForRegion(final String regionName)
    {
        Ec2Client client = clientsByRegion.get(regionName);
        if (client == null)
        {
            UrlConnectionHttpClient.Builder httpClientBuilder = UrlConnectionHttpClient.builder();
            if (proxyConfig != null)
            {
                httpClientBuilder.proxyConfiguration(proxyConfig);
            }

            client = Ec2Client.builder().httpClientBuilder(httpClientBuilder)
                              .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsConfiguration.getAccessKey(),
                                                                                                               awsConfiguration.getSecretKey())))
                              .region(software.amazon.awssdk.regions.Region.of(regionName)).build();

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
    protected Instance waitForInstanceState(final Region region, final Instance instance, final InstanceStateName state, final long timeout)
        throws Exception
    {
        final long deadline = System.currentTimeMillis() + timeout;
        final String instanceId = instance.instanceId();
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
                            instance.state().name().toString() + "'");
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
        return new ArrayList<>(getClient(region).describeAvailabilityZones().availabilityZones());
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
        final List<SecurityGroup> secGroups = new ArrayList<>(getClient(region).describeSecurityGroups().securityGroups());

        // sort the security groups by group name
        Collections.sort(secGroups, new Comparator<SecurityGroup>()
        {
            @Override
            public int compare(final SecurityGroup i1, final SecurityGroup i2)
            {
                final String d1 = StringUtils.defaultString(i1.groupName().toLowerCase());
                final String d2 = StringUtils.defaultString(i2.groupName().toLowerCase());
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
        final DescribeImagesRequest describeImagesRequest = DescribeImagesRequest.builder().owners("self", "614612213257")
                                                                                 .filters(Filter.builder().name("architecture")
                                                                                                .values("x86_64").build())
                                                                                 .imageIds(imageIds).build();
        final List<Image> images = new ArrayList<>(getClient(region).describeImages(describeImagesRequest).images());

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
        final DescribeKeyPairsResponse describeKeyPairsResponse = getClient(region).describeKeyPairs();
        final List<KeyPairInfo> keyPairInfos = new ArrayList<>(describeKeyPairsResponse.keyPairs());

        // sort the key pairs by key name
        Collections.sort(keyPairInfos, new Comparator<KeyPairInfo>()
        {
            @Override
            public int compare(final KeyPairInfo i1, final KeyPairInfo i2)
            {
                final String d1 = StringUtils.defaultString(i1.keyName());
                final String d2 = StringUtils.defaultString(i2.keyName());
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
            if (img.imageId().equals(imageId))
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

        for (final Instance instance : getInstances(region, tags, InstanceStateName.RUNNING))
        {
            instanceIds.add(instance.instanceId());
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
    protected Instance getInstance(final Region region, final String instanceID, final List<TagDescription> tags,
                                   final InstanceStateName state)
    {
        // lookup instance
        final List<Instance> instances = getInstances(region, new ArrayList<TagDescription>(), state);
        for (final Instance instance : instances)
        {
            if (instance.instanceId().equals(instanceID))
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
    protected List<Instance> getInstances(final Region region, final List<TagDescription> tags, final InstanceStateName state)
    {
        // build the tag filter list
        final Map<String, Filter> filters = new HashMap<String, Filter>();

        final Map<String, List<String>> tagKeyValueMap = new HashMap<>();

        for (final TagDescription tag : tags)
        {
            final String key = tag.key();
            List<String> values = tagKeyValueMap.get(key);
            if (values == null)
            {
                values = new ArrayList<>();
                tagKeyValueMap.put(key, values);
            }
            values.add(tag.value());
        }

        for (final String key : tagKeyValueMap.keySet())
        {
            filters.put(key, Filter.builder().name("tag:" + key).values(tagKeyValueMap.get(key)).build());
        }

        if (state != null)
        {
            final Filter stateFilter = Filter.builder().name("instance-state-name").values(state.toString()).build();
            filters.put("instance-state", stateFilter);
        }

        final List<Instance> instances = new ArrayList<Instance>();

        final DescribeInstancesRequest describeInstancesRequest = DescribeInstancesRequest.builder().filters(filters.values()).build();

        final DescribeInstancesResponse describeInstancesResponse = getClient(region).describeInstances(describeInstancesRequest);
        for (final Reservation reservation : describeInstancesResponse.reservations())
        {
            instances.addAll(reservation.instances());
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
        final DescribeRegionsRequest.Builder describeRegionsRequestBuilder = DescribeRegionsRequest.builder();
        describeRegionsRequestBuilder.regionNames(Arrays.asList(regionNames));
        describeRegionsRequestBuilder.allRegions(false);

        final DescribeRegionsResponse describeRegionsResponse = getClient(null).describeRegions(describeRegionsRequestBuilder.build());
        final List<Region> regions = new ArrayList<>(describeRegionsResponse.regions());

        // sort the regions by region name
        Collections.sort(regions, (r1, r2) -> {
            final String s1 = StringUtils.defaultString(r1.regionName());
            final String s2 = StringUtils.defaultString(r2.regionName());
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
        final List<Filter> filters = new ArrayList<>();
        if (availabilityZone != null)
        {
            filters.add(Filter.builder().name("availability-zone").values(Arrays.asList(availabilityZone.zoneName())).build());
        }
        if (vpc != null)
        {
            filters.add(Filter.builder().name("vpc-id").values(Arrays.asList(vpc.vpcId())).build());
        }

        final DescribeSubnetsRequest req = DescribeSubnetsRequest.builder().filters(filters).build();

        return new ArrayList<>(getClient(region).describeSubnets(req).subnets());
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
        final DescribeVpcsRequest.Builder vpcReqBuilder = DescribeVpcsRequest.builder();
        if (vpcIds != null && !vpcIds.isEmpty())
        {
            vpcReqBuilder.filters(Filter.builder().name("vpc-id").values(vpcIds).build());
        }
        return new ArrayList<>(getClient(region).describeVpcs(vpcReqBuilder.build()).vpcs());
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
        final RunInstancesRequest.Builder runInstancesRequestBuilder = RunInstancesRequest.builder();

        // check if specific subnet is desired
        if (subnet == null)
        {
            // get all subnets of given region
            final List<Subnet> subnets = getSubnets(region, null, null);
            if (!subnets.isEmpty())
            {
                // get the default subnet or (if not present) take the first one
                subnet = subnets.parallelStream().filter(s -> s.defaultForAz()).findAny().orElse(subnets.get(0));
            }
        }

        // must have any subnet now
        if (subnet == null)
        {
            throw new Exception("No subnet available to launch instances into");
        }

        runInstancesRequestBuilder.placement(Placement.builder().availabilityZone(subnet.availabilityZone()).build());
        runInstancesRequestBuilder.subnetId(subnet.subnetId());

        runInstancesRequestBuilder.imageId(image.imageId());
        runInstancesRequestBuilder.instanceType(instanceType);
        runInstancesRequestBuilder.minCount(count);
        runInstancesRequestBuilder.maxCount(count);

        if (StringUtils.isNotBlank(userData))
        {
            runInstancesRequestBuilder.userData(BinaryUtils.toBase64(userData.getBytes()));
        }

        if (securityGroupIds != null)
        {
            runInstancesRequestBuilder.securityGroupIds(securityGroupIds);
        }

        if (StringUtils.isNotBlank(keyPairName))
        {
            runInstancesRequestBuilder.keyName(keyPairName);
        }

        final RunInstancesResponse response = getClient(region).runInstances(runInstancesRequestBuilder.build());
        final List<Instance> instances = new ArrayList<>(response.instances());

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
                                  region.regionName());

                final TerminateInstancesRequest.Builder terminateInstancesRequestBuilder = TerminateInstancesRequest.builder();
                terminateInstancesRequestBuilder.instanceIds(getRunningInstanceIds(region, tags));
                getClient(region).terminateInstances(terminateInstancesRequestBuilder.build());

                System.out.println("OK.");
            }
            catch (final SdkException e)
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
        final TerminateInstancesRequest.Builder terminateInstancesRequestBuilder = TerminateInstancesRequest.builder();
        terminateInstancesRequestBuilder.instanceIds(instance.instanceId());

        getClient(region).terminateInstances(terminateInstancesRequestBuilder.build());
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
        tags.add(Tag.builder().key("Name").value(name).build());

        // build tag request
        final CreateTagsRequest.Builder createTagRequestBuilder = CreateTagsRequest.builder();
        createTagRequestBuilder.resources(ids);
        createTagRequestBuilder.tags(tags);

        // set tag
        getClient(region).createTags(createTagRequestBuilder.build());
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
            AwsServiceException lastException = null;

            try
            {
                // dummy request to describe the instances
                final DescribeInstancesRequest.Builder describeInstancesRequestBuilder = DescribeInstancesRequest.builder();
                describeInstancesRequestBuilder.instanceIds(instanceIds);

                getClient(region).describeInstances(describeInstancesRequestBuilder.build());

                // no exception, return immediately
                return;
            }
            catch (final AwsServiceException e)
            {
                // check if some instances were reported as non-existing
                if ("InvalidInstanceID.NotFound".equals(e.awsErrorDetails().errorCode()))
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
            instanceIds.add(instance.instanceId());
        }

        return instanceIds;
    }

    /**
     * Shuts down the EC2 clients that were potentially created by {@link #getClient(Region)}.
     *
     * @see Ec2Client#close()
     */
    public void shutdown()
    {
        clientsByRegion.values().forEach(Ec2Client::close);
    }
}
