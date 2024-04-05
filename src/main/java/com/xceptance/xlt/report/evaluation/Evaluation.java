package com.xceptance.xlt.report.evaluation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        private double pointsPercentage;

        @XStreamAsAttribute
        private int points;

        @XStreamAsAttribute
        private int totalPoints;

        @XStreamAsAttribute
        private boolean testFailed;

        private List<Group> groups;

        private String message;

        private String error;

        private String rating;

        public String getMessage()
        {
            return message;
        }

        void setMessage(final String message)
        {
            this.message = Objects.requireNonNull(message);
        }

        public String getError()
        {
            return error;
        }

        void setError(final String error)
        {
            this.error = Objects.requireNonNull(error);
        }

        public int getPoints()
        {
            return points;
        }

        void setPoints(final int points)
        {
            this.points = points;
        }

        public double getPointsPercentage()
        {
            return pointsPercentage;
        }

        void setPointsPercentage(final double pointsPercentage)
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

        public int getTotalPoints()
        {
            return totalPoints;
        }

        void setTotalPoints(final int totalPoints)
        {
            this.totalPoints = totalPoints;
        }

        void addGroup(final Group group)
        {
            if (groups == null)
            {
                groups = new LinkedList<>();
            }
            groups.add(Objects.requireNonNull(group));
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

        Group(final GroupDefinition definition)
        {
            this.definition = definition;
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

        public void computePoints()
        {
            if (!definition.isEnabled())
            {
                return;
            }

            Integer firstMatch = null, lastMatch = null;
            int maxPoints = 0, sumPoints = 0, sumPointsMatching = 0;
            for (final Rule rule : rules)
            {
                // rules must be enabled in order to participate in point calculation
                if (!rule.definition.isEnabled())
                {
                    continue;
                }

                final int rulePoints = rule.points;
                final int rulePointsMax = rule.definition.getPoints();
                maxPoints = Math.max(maxPoints, rulePointsMax);
                if (rule.status.isPassed())
                {
                    if (firstMatch == null)
                    {
                        firstMatch = Integer.valueOf(rulePoints);
                    }
                    lastMatch = Integer.valueOf(rulePoints);

                    sumPointsMatching += rulePoints;
                }

                sumPoints += rulePointsMax;
            }

            switch (definition.getPointSource())
            {
                case FIRST:
                    points = Optional.ofNullable(firstMatch).orElse(0);
                    totalPoints = maxPoints;
                    break;
                case LAST:
                    points = Optional.ofNullable(lastMatch).orElse(0);
                    totalPoints = maxPoints;
                    break;
                case ALL:
                    points = sumPointsMatching;
                    totalPoints = sumPoints;
                    break;
            }

        }

    }

    @XStreamAlias("rule")
    public static class Rule
    {
        private transient final RuleDefinition definition;

        private transient final boolean groupEnabled;

        private final List<Check> checks = new LinkedList<>();

        private Status status = Status.SKIPPED;

        private String message;

        @XStreamAsAttribute
        private int points;

        @XStreamAsAttribute
        @XStreamAlias("ref-id")
        private final String id;

        Rule(final RuleDefinition definition, final boolean groupEnabled)
        {
            this.definition = definition;
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
            this.status = status;
        }

        public String getMessage()
        {
            return message;
        }

        void setMessage(final String message)
        {
            this.message = message;
        }

        public int getPoints()
        {
            return points;
        }

        void setPoints(final int points)
        {
            this.points = Math.max(0, Math.min(definition.getPoints(), points));
        }

        void conclude()
        {
            if (!isEnabled())
            {
                return;
            }

            Status lastStatus = null;
            for (final Check c : checks)
            {

                if (c.status.isSkipped())
                {
                    continue;
                }
                if (lastStatus == null || !c.status.isPassed())
                {
                    lastStatus = c.status;
                    if (c.status.isError())
                    {
                        message = c.errorMessage;
                        break;
                    }
                }
            }

            if (lastStatus != null)
            {
                status = lastStatus;
                if (lastStatus.isPassed())
                {
                    message = definition.getSuccessMessage();
                    points = definition.getPoints();
                }
                else if (lastStatus.isFailed())
                {
                    message = definition.getFailMessage();
                }
            }

        }

        public boolean isEnabled()
        {
            return groupEnabled && definition.isEnabled();
        }

        @XStreamAlias("check")
        public static class Check
        {
            @XStreamAsAttribute
            private final int index;

            private transient final RuleDefinition.Check definition;

            private transient final boolean ruleEnabled;

            private Status status = Status.SKIPPED;

            private String errorMessage;

            private String value;

            Check(final RuleDefinition.Check definition, final boolean ruleEnabled)
            {
                this.definition = definition;
                this.index = definition.getIndex();
                this.ruleEnabled = ruleEnabled;
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
                this.errorMessage = errorMessage;
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
