package com.xceptance.xlt.report.evaluation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("evaluation")
public class Evaluation
{
    public static class Result
    {
        @XStreamAsAttribute
        private Double pointsPercentage;

        @XStreamAsAttribute
        private Integer points;

        @XStreamAsAttribute
        private Integer totalPoints;

        @XStreamAsAttribute
        private boolean testFailed;

        private List<Group> groups;

        private String error;

        private String rating;

        public String getError()
        {
            return error;
        }

        void setError(final String error)
        {
            this.error = Objects.requireNonNull(error);
        }

        public Integer getPoints()
        {
            return points;
        }

        void setPoints(final Integer points)
        {
            this.points = points;
        }

        public Double getPointsPercentage()
        {
            return pointsPercentage;
        }

        void setPointsPercentage(final Double pointsPercentage)
        {
            this.pointsPercentage = pointsPercentage;
        }

        public boolean isTestFailed()
        {
            return testFailed;
        }

        void setTestFailed(final boolean testFailed)
        {
            this.testFailed = testFailed;
        }

        public Integer getTotalPoints()
        {
            return totalPoints;
        }

        void setTotalPoints(final Integer totalPoints)
        {
            this.totalPoints = totalPoints;
        }

        void addGroup(final Group group)
        {
            Objects.requireNonNull(group);
            if (groups == null)
            {
                groups = new LinkedList<>();
            }
            groups.add(group);
        }

        public List<Group> getGroups()
        {
            if (groups == null)
            {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(groups);
        }

        public String getRating()
        {
            return rating;
        }

        void setRating(final String rating)
        {
            this.rating = rating;
        }

    }

    public final Configuration configuration;

    @XStreamAlias("outcome")
    public final Evaluation.Result result = new Evaluation.Result();

    Evaluation(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    static Evaluation error(final Throwable t)
    {
        final Evaluation r = new Evaluation(null);
        final String errMsg = ExceptionUtils.stream(t).map(Throwable::getMessage).collect(Collectors.joining(" -> "));
        r.result.setError(errMsg);
        return r;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }

    @XStreamAlias("group")
    public static class Group
    {
        private final transient GroupDefinition definition;

        @XStreamAsAttribute
        @XStreamAlias("ref-id")
        private final String id;

        private final List<Rule> rules = new LinkedList<>();

        @XStreamAsAttribute
        private int points;

        @XStreamAsAttribute
        private int totalPoints;

        private String message;

        @XStreamAlias("result")
        private Status status = Status.SKIPPED;

        @XStreamAsAttribute
        private boolean testFailed;

        Group(final GroupDefinition definition)
        {
            this.definition = Objects.requireNonNull(definition, "Group definition must not be null");
            this.id = definition.getId();
        }

        public GroupDefinition getDefinition()
        {
            return definition;
        }

        public String getId()
        {
            return id;
        }

        public List<Rule> getRules()
        {
            return Collections.unmodifiableList(rules);
        }

        void addRule(final Rule rule)
        {
            rules.add(Objects.requireNonNull(rule));
        }

        public int getPoints()
        {
            return points;
        }

        void setPoints(final int points)
        {
            this.points = Math.max(0, points);
        }

        public int getTotalPoints()
        {
            return totalPoints;
        }

        void setTotalPoints(final int totalPoints)
        {
            this.totalPoints = Math.max(0, totalPoints);
        }

        public String getMessage()
        {
            return message;
        }

        void setMessage(final String message)
        {
            this.message = StringUtils.trimToNull(message);
        }

        void setStatus(final Status status)
        {
            this.status = Objects.requireNonNull(status);
        }

        public Status getStatus()
        {
            return status;
        }

        void setTestFailed()
        {
            this.testFailed = true;
        }

        public boolean isEnabled()
        {
            return definition.isEnabled();
        }

        TestFailTrigger getFailsOn()
        {
            return Optional.ofNullable(definition.getFailsOn()).orElse(TestFailTrigger.NOTPASSED);
        }

        boolean mayFailTest()
        {
            return isEnabled() && definition.isFailsTest() && getFailsOn().isTriggeredBy(status);
        }
    }

    @XStreamAlias("rule")
    public static class Rule
    {
        private transient final RuleDefinition definition;

        private transient final boolean groupEnabled;

        private final List<Check> checks = new LinkedList<>();

        @XStreamAlias("result")
        private Status status = Status.SKIPPED;

        private String message;

        @XStreamAsAttribute
        private int points;

        @XStreamAsAttribute
        @XStreamAlias("ref-id")
        private final String id;

        @XStreamAsAttribute
        private boolean testFailed;

        Rule(final RuleDefinition definition, final boolean groupEnabled)
        {
            this.definition = Objects.requireNonNull(definition, "Rule definition must not be null");
            this.groupEnabled = groupEnabled;

            this.id = definition.getId();
        }

        public String getId()
        {
            return id;
        }

        public RuleDefinition getDefinition()
        {
            return definition;
        }

        public List<Check> getChecks()
        {
            return Collections.unmodifiableList(checks);
        }

        void addCheck(final Check check)
        {
            checks.add(Objects.requireNonNull(check));
        }

        public Status getStatus()
        {
            return status;
        }

        void setStatus(final Status status)
        {
            this.status = Objects.requireNonNull(status);
        }

        public String getMessage()
        {
            return message;
        }

        void setMessage(final String message)
        {
            this.message = StringUtils.trimToNull(message);
        }

        public int getPoints()
        {
            return points;
        }

        void setPoints(final int points)
        {
            this.points = Math.max(0, Math.min(definition.getPoints(), points));
        }

        public boolean isEnabled()
        {
            return groupEnabled && definition.isEnabled();
        }

        TestFailTrigger getFailsOn()
        {
            return Optional.ofNullable(definition.getFailsOn()).orElse(TestFailTrigger.NOTPASSED);
        }

        boolean mayFailTest()
        {
            return isEnabled() && definition.isFailsTest() && getFailsOn().isTriggeredBy(status);
        }

        void setTestFailed()
        {
            this.testFailed = true;
        }

        @XStreamAlias("check")
        public static class Check
        {
            @XStreamAsAttribute
            private final int index;

            private transient final RuleDefinition.Check definition;

            private transient final boolean ruleEnabled;

            @XStreamAlias("result")
            private Status status = Status.SKIPPED;

            private String errorMessage;

            private String value;

            Check(final RuleDefinition.Check definition, final boolean ruleEnabled)
            {
                this.definition = Objects.requireNonNull(definition, "Check definition must not be null");
                this.ruleEnabled = ruleEnabled;

                this.index = definition.getIndex();
            }

            public int getIndex()
            {
                return index;
            }

            public RuleDefinition.Check getDefinition()
            {
                return definition;
            }

            public Status getStatus()
            {
                return status;
            }

            void setStatus(final Status status)
            {
                this.status = Objects.requireNonNull(status);
            }

            public String getErrorMessage()
            {
                return errorMessage;
            }

            void setErrorMessage(final String errorMessage)
            {
                this.errorMessage = StringUtils.trimToNull(errorMessage);
            }

            public String getValue()
            {
                return value;
            }

            void setValue(final String value)
            {
                this.value = value;
            }

            public boolean isEnabled()
            {
                return ruleEnabled && definition.isEnabled();
            }
        }

    }
}
