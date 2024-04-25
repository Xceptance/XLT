<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!--
    This file contains the description sections for the report as well as
    the headlines for the section.

    The format is always <h2> as main headline, <h3> as description headline
    and some <p> or <ul> as text.
-->

<xsl:template name="headline-scorecard">
<h2>Scorecard Result</h2>
</xsl:template>

<xsl:template name="description-scorecard">
<div class="description">
    <xsl:variable name="gid" select="concat('scorecard-',generate-id())" />
    <p>A performance test scorecard is a quick and easy way to evaluate a test result.
        <xsl:call-template name="show-n-hide">
            <xsl:with-param name="gid" select="$gid"/>
        </xsl:call-template>
    </p>
    <div id="more-{$gid}" class="more">
        <p>Based on the total score and the maximum possible score, a percentage score is calculated that serves as the final verdict for the test.</p>
        <p>This summary page gives you the insight you need into the assessment and helps you to derive recommendations for next steps. It also helps you identifying areas for improvement.</p>
    </div>
</div>
</xsl:template>

<xsl:template name="headline-ratings">
<h2>Rating</h2>
</xsl:template>

<xsl:template name="description-ratings">
<div class="description">
    <xsl:variable name="gid" select="concat('ratings-',generate-id())" />
    <p>A rating defines the result of the scoring. It is basically a quick verdict on the created scorecard.
        <xsl:call-template name="show-n-hide">
            <xsl:with-param name="gid" select="$gid"/>
        </xsl:call-template>
    </p>
    <div id="more-{$gid}" class="more">
        <p>A score defines a verbal test result and can also mark the test as failed if necessary. The score uses the percentage value of the points achieved versus the maximum points achievable to calculate the score. The rating value defines the upper limit for the application of this score.</p>
    </div>
</div>
</xsl:template>

<xsl:template name="headline-rules">
<h2>Rules</h2>
</xsl:template>

<xsl:template name="description-rules">
<div class="description">
    <xsl:variable name="gid" select="concat('rules-', generate-id())" />
    <p>Rules consist of data checks on the test result. A rule must be assigned to at least one group to be effective.
        <xsl:call-template name="show-n-hide">
            <xsl:with-param name="gid" select="$gid" />
        </xsl:call-template>
    </p>
    <div id="more-{$gid}" class="more">
        <p>A rule passes if all its conditions evaluate to <code>true</code>, otherwise it fails.</p>
        <p>If a rule is marked as "Error", the evaluation statement was incorrect and the rule is ignored. A rule can mark the entire test as failed, regardless of the rating result, if the "fails test" attribute is set to <code>true</code>.</p>
    </div>
</div>
</xsl:template>

<xsl:template name="headline-rulechecks">
<h3>Rule Checks</h3>
</xsl:template>

<xsl:template name="description-rulechecks">
<div class="description">
    <xsl:variable name="gid" select="concat('rulechecks-',generate-id())" />
    <p>Rule checks act as queries against the load test result data.
        <xsl:call-template name="show-n-hide">
            <xsl:with-param name="gid" select="$gid" />
        </xsl:call-template>
    </p>
    <div id="more-{$gid}" class="more">
        <p>Any data provided by XLT in its XML result format can be used to create rule checks. A check must be associated with a rule to be effective, and it can be used in more than one rule. A check consists of a selector against the XML document and a condition that it must satisfy. This table lists the details of the rule and its results within the group to which it is assigned.</p>
    </div>
</div>
</xsl:template>

<xsl:template name="headline-groups">
<h2>Groups</h2>
</xsl:template>

<xsl:template name="description-groups">
<div class="description">
    <p>Groups define which rules are evaluated and how the overall result of multiple related rules is determined. A rule may be used in more than one group. Each group requires at least one rule to be considered.</p>
</div>
</xsl:template>

<!-- The show and hide part -->
<xsl:template name="show-n-hide">
    <xsl:param name="gid"/>
    <span id="more-{$gid}-show" onclick="$('#more-{$gid}').show();$('#more-{$gid}-hide').show(); $(this).hide();"
        class="link more-show">More...</span>
    <span id="more-{$gid}-hide" onclick="$('#more-{$gid}').hide();$('#more-{$gid}-show').show(); $(this).hide();"
        class="link more-hide">Hide...</span>
</xsl:template>

</xsl:stylesheet>
