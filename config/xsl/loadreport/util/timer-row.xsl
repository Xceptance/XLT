<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<xsl:template name="timer-row">
		<xsl:param name="type" />

		<xsl:variable name="gid" select="generate-id(.)" />

		<tr>
			<!-- name -->
			<td class="key colgroup1 forcewordbreak">
				<a>
					<xsl:attribute name="href">#chart-<xsl:value-of
						select="$gid" /></xsl:attribute>
					<xsl:attribute name="data-id">tableEntry-<xsl:value-of
						select="$gid" /></xsl:attribute>
					<xsl:if test="count(urls) &gt; 0">
						<!-- title and class only for requests with urls -->
						<xsl:attribute name="data-rel">#url-listing-<xsl:value-of
							select="$gid" /></xsl:attribute>
						<xsl:attribute name="class">cluetip</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="name" />
				</a>
				<xsl:text></xsl:text>
				<xsl:if test="count(urls) &gt; 0">
					<div id="url-listing-{$gid}" class="cluetip-data">
						<h4>
							<xsl:value-of select="format-number(urls/total, '#,##0')" />
							<xsl:text> distinct URL(s)</xsl:text>
						</h4>
						<ul class="urls">
							<xsl:for-each select="urls/list/string">
								<xsl:variable name="oddoreven">
									<xsl:choose>
										<xsl:when test="position() mod 2 = 0">
											<xsl:text>even</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:text>odd</xsl:text>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
								<li class="{$oddoreven}">
									<a href="{.}" target="_blank">
										<xsl:value-of select="." />
									</a>
								</li>
							</xsl:for-each>
						</ul>
					</div>
				</xsl:if>
			</td>

			<!-- count -->
			<td class="value number">
				<xsl:value-of select="format-number(count, '#,##0')" />
			</td>

            <xsl:choose>
                <xsl:when test="$type = 'request'">
                    <!-- distinct -->
                    <td class="value number">
                        <xsl:value-of select="format-number(urls/total, '#,##0')" />
                    </td>

                    <!-- count per sec -->
                    <td class="value number">
                        <xsl:value-of select="format-number(countPerSecond, '#,##0.0')" />
                    </td>

                    <!-- count per hour -->
                    <td class="value number">
                        <xsl:value-of select="format-number(countPerHour, '#,##0')" />
                    </td>
                </xsl:when>
                <xsl:otherwise>
                    <!-- count per sec -->
                    <td class="value number">
                        <xsl:value-of select="format-number(countPerSecond, '#,##0.0')" />
                    </td>

                    <!-- count per hour -->
                    <td class="value number">
                        <xsl:value-of select="format-number(countPerHour, '#,##0')" />
                    </td>

                    <!-- count per day -->
                    <td class="value number">
                        <xsl:value-of select="format-number(countPerDay, '#,##0')" />
                    </td>
                </xsl:otherwise>
            </xsl:choose>

			<!-- errors -->
			<td class="value number colgroup1">
				<xsl:if test="errors &gt; 0">
					<xsl:attribute name="class">value number colgroup1 error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="format-number(errors, '#,##0')" />
			</td>

			<!-- % errors -->
			<xsl:variable name="error-percentage">
				<xsl:call-template name="percentage">
					<xsl:with-param name="n1" select="count" />
					<xsl:with-param name="n2" select="errors" />
				</xsl:call-template>
			</xsl:variable>
			<td class="value number colgroup1">
				<xsl:if test="errors &gt; 0">
					<xsl:attribute name="class">value number colgroup1 error</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="format-number($error-percentage, '#,##0.00')" />
				<xsl:text>%</xsl:text>
			</td>

			<!-- events -->
			<xsl:if test="$type = 'transaction'">
				<td class="value number colgroup1">
					<xsl:if test="events &gt; 0">
						<xsl:attribute name="class">value number colgroup1 event</xsl:attribute>
					</xsl:if>
					<xsl:value-of select="format-number(events, '#,##0')" />
				</td>
			</xsl:if>

			<!-- store the matching colorization config -->
			<xsl:variable name="colorGroup" select="colorizationGroupName" />
			<xsl:variable name="colorizationConfig"
				select="/testreport/testReportConfig/requestTableColorization/colorization[@groupName=$colorGroup]" />

			<!-- mean -->
			<xsl:variable name="classNames" select="string('value number')" />
			<td class="{$classNames}">
				<xsl:if test="$colorizationConfig">
					<xsl:call-template name="colorize">
						<xsl:with-param name="classNames" select="$classNames" />
						<xsl:with-param name="runtime" select="mean" />
						<xsl:with-param name="targetAverage"
							select="number($colorizationConfig/rules/rule[@id='mean']/@target)" />
						<xsl:with-param name="targetFrom"
							select="number($colorizationConfig/rules/rule[@id='mean']/@from)" />
						<xsl:with-param name="targetTo"
							select="number($colorizationConfig/rules/rule[@id='mean']/@to)" />
					</xsl:call-template>
				</xsl:if>

				<!-- pretty print -->
				<xsl:value-of select="format-number(mean, '#,##0')" />
			</td>

			<!-- min -->
			<xsl:variable name="classNames" select="string('value number')" />
			<td class="{$classNames}">
				<xsl:if test="$colorizationConfig">
					<xsl:call-template name="colorize">
						<xsl:with-param name="classNames" select="$classNames" />
						<xsl:with-param name="runtime" select="min" />
						<xsl:with-param name="targetAverage"
							select="number($colorizationConfig/rules/rule[@id='min']/@target)" />
						<xsl:with-param name="targetFrom"
							select="number($colorizationConfig/rules/rule[@id='min']/@from)" />
						<xsl:with-param name="targetTo"
							select="number($colorizationConfig/rules/rule[@id='min']/@to)" />
					</xsl:call-template>
				</xsl:if>

				<xsl:value-of select="format-number(min, '#,##0')" />
			</td>

			<!-- max -->
			<xsl:variable name="classNames" select="string('value number')" />
			<td class="{$classNames}">
				<xsl:if test="$colorizationConfig">
					<xsl:call-template name="colorize">
						<xsl:with-param name="classNames" select="$classNames" />
						<xsl:with-param name="runtime" select="max" />
						<xsl:with-param name="targetAverage"
							select="number($colorizationConfig/rules/rule[@id='max']/@target)" />
						<xsl:with-param name="targetFrom"
							select="number($colorizationConfig/rules/rule[@id='max']/@from)" />
						<xsl:with-param name="targetTo"
							select="number($colorizationConfig/rules/rule[@id='max']/@to)" />
					</xsl:call-template>
				</xsl:if>

				<xsl:value-of select="format-number(max, '#,##0')" />
			</td>

			<!-- deviation -->
			<td class="value number">
				<xsl:value-of select="format-number(deviation, '#,##0')" />
			</td>

			<!-- runtime percentiles -->

			<xsl:for-each select="percentiles/*">
				<xsl:variable name="id" select="name()" />
				<xsl:variable name="classNames" select="string('value number colgroup1')" />
				<td class="{$classNames}">
					<xsl:if test="$colorizationConfig">
						<xsl:call-template name="colorize">
							<xsl:with-param name="classNames" select="$classNames" />
							<xsl:with-param name="runtime" select="number(text())" />
							<xsl:with-param name="targetAverage"
								select="number($colorizationConfig/rules/rule[@type='percentile' and @id=$id]/@target)" />
							<xsl:with-param name="targetFrom"
								select="number($colorizationConfig/rules/rule[@type='percentile' and @id=$id]/@from)" />
							<xsl:with-param name="targetTo"
								select="number($colorizationConfig/rules/rule[@type='percentile' and @id=$id]/@to)" />
						</xsl:call-template>
					</xsl:if>

					<xsl:value-of select="format-number(current(), '#,##0')" />
				</td>
			</xsl:for-each>

			<!-- apdex -->
			<xsl:if test="$type = 'action'">
				<xsl:variable name="apdexColor">
					<xsl:call-template name="convert-apdex-to-color">
						<xsl:with-param name="apdex" select="apdex/value" />
					</xsl:call-template>
				</xsl:variable>
				<td class="value number apdex {$apdexColor}">
					<xsl:value-of select="apdex/longValue" />
				</td>
			</xsl:if>

			<!-- runtime segmentation -->
			<xsl:if test="$type = 'request'">
				<xsl:for-each select="countPerInterval/int">
					<xsl:variable name="position" select="position()" />
					<xsl:variable name="count" select="count(../int)" />
					<xsl:variable name="id"
						select="/testreport/testReportConfig/runtimeIntervals/interval[$position]/@to" />
					<xsl:variable name="classNames" select="string('value number')" />

					<td class="{$classNames}">
						<xsl:variable name="percentage">
							<xsl:call-template name="percentage">
								<xsl:with-param name="n1" select="../../count" />
								<xsl:with-param name="n2" select="current()" />
							</xsl:call-template>
						</xsl:variable>

						<xsl:if test="$colorizationConfig">
							<xsl:call-template name="colorize">
								<xsl:with-param name="classNames" select="$classNames" />
								<xsl:with-param name="inverted" select="not($count = $position)" />
								<xsl:with-param name="runtime" select="number($percentage)" />
								<xsl:with-param name="targetAverage"
									select="number($colorizationConfig/rules/rule[@type='segmentation' and @id=$id]/@target)" />
								<xsl:with-param name="targetFrom"
									select="number($colorizationConfig/rules/rule[@type='segmentation' and @id=$id]/@from)" />
								<xsl:with-param name="targetTo"
									select="number($colorizationConfig/rules/rule[@type='segmentation' and @id=$id]/@to)" />
							</xsl:call-template>
						</xsl:if>

						<span>
							<xsl:attribute name="title">
                                <xsl:value-of
								select="format-number(current(), '#,##0')" />
                                <xsl:text> (</xsl:text>
                                <xsl:value-of
								select="format-number($percentage, '#,##0.00')" />
                                <xsl:text>%)</xsl:text>
                            </xsl:attribute>
							<xsl:value-of select="format-number($percentage, '#,##0.00')" />
							<xsl:text>%</xsl:text>
						</span>
					</td>
				</xsl:for-each>
			</xsl:if>
		</tr>

	</xsl:template>

	<!-- Template to colorize request runtimes in the request table -->
	<xsl:template name="colorize">
		<!-- template params -->
		<xsl:param name="classNames" />
		<xsl:param name="runtime" />
		<xsl:param name="targetAverage" />
		<xsl:param name="targetFrom" />
		<xsl:param name="targetTo" />
		<xsl:param name="inverted" select="false()" />

		<xsl:variable name="positivePrefix">
			<xsl:choose>
				<xsl:when test="not($inverted)">
					<xsl:value-of select="string('p')"></xsl:value-of>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="string('n')"></xsl:value-of>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="negativePrefix">
			<xsl:choose>
				<xsl:when test="not($inverted)">
					<xsl:value-of select="string('n')"></xsl:value-of>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="string('p')"></xsl:value-of>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<!-- find and set the color class attribute -->
		<xsl:if
			test="$targetAverage &gt;= 0 and $targetFrom &gt;= 0 and $targetTo &gt;= 0">
			<xsl:choose>
				<xsl:when test="$runtime &gt; $targetAverage">
					<xsl:variable name="percent"
						select="floor(($runtime - $targetAverage) * (100 div ($targetTo - $targetAverage)) )" />
					<xsl:choose>
						<xsl:when test="$percent &gt;= 100">
							<xsl:call-template name="extendClass">
								<xsl:with-param name="classNames"
									select="concat($classNames, ' ', concat($negativePrefix, '100'))"></xsl:with-param>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$percent &lt;= 0">
									<xsl:call-template name="extendClass">
										<xsl:with-param name="classNames"
											select="concat($classNames, ' ', concat($negativePrefix, '0'))"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="extendClass">
										<xsl:with-param name="classNames"
											select="concat($classNames, ' ', concat($negativePrefix, $percent))"></xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="percent"
						select="floor(($runtime - $targetAverage) * (100 div ($targetFrom - $targetAverage)))" />
					<xsl:choose>
						<xsl:when test="$percent &gt;= 100">
							<xsl:call-template name="extendClass">
								<xsl:with-param name="classNames"
									select="concat($classNames, ' ', concat($positivePrefix, '100'))"></xsl:with-param>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$percent &lt;= 0">
									<xsl:call-template name="extendClass">
										<xsl:with-param name="classNames"
											select="concat($classNames, ' ', concat($positivePrefix, '0'))"></xsl:with-param>
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="extendClass">
										<xsl:with-param name="classNames"
											select="concat($classNames, ' ', concat($positivePrefix, $percent))"></xsl:with-param>
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<!-- Extend the class attribute with the associated runtime color -->
	<xsl:template name="extendClass">
		<xsl:param name="classNames" />

		<xsl:attribute name="class">
			<xsl:value-of select="concat($classNames, ' ', 'colorized')" />
		</xsl:attribute>
	</xsl:template>

</xsl:stylesheet>