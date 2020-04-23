/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.criteria;

/**
 * Criterion validation result.
 */
public final class CriterionResult
{
    /**
     * Known states.
     */
    static enum Status
    {
     PASSED,
     FAILED,
     ERROR,
     SKIPPED;
    }

    /**
     * Criterion's ID.
     */
    private final String id;

    /**
     * Criterion's status.
     */
    private final Status status;

    /**
     * Criterion's failure reason.
     */
    private final String message;

    private CriterionResult(final String id, final Status status)
    {
        this(id, status, null);
    }

    private CriterionResult(final String id, final Status status, final String message)
    {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    /**
     * Short-hand for a passed criterion.
     * 
     * @param criterionId
     *            the ID of the passed criterion
     * @return criterion validation result
     */
    public static CriterionResult passed(final String criterionId)
    {
        return new CriterionResult(criterionId, Status.PASSED);
    }

    /**
     * Short-hand for a skipped criterion.
     * 
     * @param criterionId
     *            the ID of the skipped criterion
     * @return criterion validation result
     */
    public static CriterionResult skipped(final String criterionId)
    {
        return new CriterionResult(criterionId, Status.SKIPPED);
    }

    /**
     * Short-hand for a failed criterion.
     * 
     * @param criterionId
     *            the ID of the failed criterion
     * @param failureMessage
     *            failure message (as given in criteria definition or a generated one)
     * @return criterion validation result
     */
    public static CriterionResult failed(final String criterionId, final String failureMessage)
    {
        return new CriterionResult(criterionId, Status.FAILED, failureMessage);
    }

    /**
     * Short-hand for a criterion that couldn't be validated at all.
     * 
     * @param criterionId
     *            the ID of the criterion
     * @param errorMessage
     *            the error message
     * @return criterion validation result
     */
    public static CriterionResult error(final String criterionId, final String errorMessage)
    {
        return new CriterionResult(criterionId, Status.ERROR, errorMessage);
    }

    /**
     * Returns the ID of the criterion.
     * 
     * @return the criterion's ID
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the validation status.
     * 
     * @return the status
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * Returns the failure reason.
     * 
     * @return the failure reason or {@code null} if passed
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Returns whether or not this criterion has passed validation.
     * 
     * @return <code>true</code> if this criterion passed validation (condition is fulfilled or validation was skipped),
     *         <code>false</code> otherwise
     */
    public boolean hasPassed()
    {
        return status == Status.PASSED || status == Status.SKIPPED;
    }
}
