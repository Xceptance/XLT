<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

	<!-- Magic to group errors by their message for counting them later on. -->
	<xsl:key name="errorsByMessage" match="errors/error" use="message" />

	<xsl:template name="errors">
		<xsl:param name="rootNode" />

		<div class="section" id="error-summary">
			<xsl:call-template name="headline-error-summary" />

			<div class="content">
				<xsl:call-template name="description-error-summary" />

				<div class="data">

					<div class="charts">
						<div class="chart">
							<img src="charts/Errors.png" alt="Errors" />
						</div>
					</div>

					<!-- Request Error Charts -->
					<h3 id="request-errors">Request Errors</h3>
					<div class="description">
						<xsl:variable name="gid"
							select="concat('requestErrorCharts', generate-id(.))" />
						<p>
							This section displays the request errors grouped by response
							code.
							<xsl:call-template name="show-n-hide">
								<xsl:with-param name="gid" select="$gid" />
							</xsl:call-template>
						</p>
						<div id="more-{$gid}" class="more">
							<p>
								The chart creation can be configured by setting the
								corresponding report generator properties. (See
								<b>reportgenerator.properties</b>
								file for more details)
							</p>
						</div>
					</div>

					<xsl:choose>
						<xsl:when test="count($rootNode/requestErrorOverviewCharts/chart) > 0">
							<div class="charts">
								<xsl:for-each select="$rootNode/requestErrorOverviewCharts/chart">
									<xsl:sort select="id" data-type="number" />
									<div class="chart">
										<img>
											<xsl:attribute name="src">
				                            	<xsl:value-of
												select="concat('charts/errors/r',string(./id),'.png')"></xsl:value-of>
				                            </xsl:attribute>
											<xsl:attribute name="alt">Response Errors</xsl:attribute>
										</img>
									</div>
								</xsl:for-each>
							</div>
						</xsl:when>
						<xsl:otherwise>
							<p class="text-center">
								There are no charts to show.
							</p>
						</xsl:otherwise>
					</xsl:choose>

					<!-- Transaction Error Overview -->
					<h3 id="transaction-error-overview">Transaction Error Overview</h3>
					<table
						class="table-autosort:1 table-autosort-order:desc table-autostripe table-stripeclass:odd">
						<thead>
							<tr>
								<th class="table-sortable:alphanumeric">Error Message</th>
								<th class="table-sortable:numeric">Count</th>
								<th class="table-sortable:numeric">Percentage</th>
							</tr>
						</thead>
						<xsl:choose>
							<xsl:when test="count($rootNode/error) > 0">
								<xsl:variable name="totalErrorCount">
									<xsl:value-of select="sum($rootNode/error/count)" />
								</xsl:variable>
								<xsl:variable name="errorRepresentativesWithDistinctMessages" select="errors/error[generate-id() = generate-id(key('errorsByMessage', message)[1])]" />
								<xsl:variable name="countDistinctErrorMessages" select="count($errorRepresentativesWithDistinctMessages)" />
								<tfoot>
									<tr class="totals">
                                        <xsl:call-template name="create-totals-td">
    										<xsl:with-param name="rows-in-table" select="$countDistinctErrorMessages" />
    									</xsl:call-template>
                                        
                                        <td class="value number">
                                            <xsl:value-of select="format-number($totalErrorCount, '#,##0')" />
                                        </td>
                                        <td class="value number">
                                            <xsl:value-of select="format-number(1, '#0.0%')" />
                                        </td>
                                    </tr>
								</tfoot>
								<tbody>
									<!-- Here the grouping magic continues ... -->
									<xsl:for-each
										select="$errorRepresentativesWithDistinctMessages">
										<xsl:sort select="message" />

										<xsl:variable name="errorCountByMessage">
											<xsl:value-of select="sum(key('errorsByMessage', message)/count)" />
										</xsl:variable>

										<xsl:variable name="errorMessage">
											<xsl:value-of select="message" />
										</xsl:variable>

										<xsl:variable name="overviewChartID">
											<xsl:value-of
												select="string($rootNode/transactionErrorOverviewCharts/chart[./title = $errorMessage]/id)" />
										</xsl:variable>

										<tr>
											<td class="value text forcewordbreak">
												<xsl:choose>
													<xsl:when test="string-length($overviewChartID) > 0">
														<a>
															<xsl:attribute name="id">
		                                            			<xsl:value-of
																select="concat('tableEntry-',$overviewChartID)" />
		                                            		</xsl:attribute>
															<xsl:attribute name="href">
		                                            			<xsl:value-of
																select="concat('#',$overviewChartID)" />
		                                            		</xsl:attribute>
															<xsl:value-of select="message" />
														</a>
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="message" />
													</xsl:otherwise>
												</xsl:choose>
											</td>
											<td class="value number count">
												<xsl:value-of select="format-number($errorCountByMessage, '#,##0')" />
											</td>
											<td class="value number count">
												<xsl:value-of
													select="format-number($errorCountByMessage div $totalErrorCount, '#0.0%')" />
											</td>
										</tr>
									</xsl:for-each>
								</tbody>
							</xsl:when>
							<xsl:otherwise>
								<tfoot>
									<tr>
										<td></td>
										<td></td>
										<td></td>
									</tr>
								</tfoot>
								<tbody>
									<tr>
										<td class="value text" colspan="3">There are no values to show
											in this table.</td>
									</tr>
								</tbody>
							</xsl:otherwise>
						</xsl:choose>
					</table>

					<!-- Transaction Error Overview Charts -->
					<div class="description">
						<xsl:variable name="gid"
							select="concat('transactionOverview', generate-id(.))" />
						<xsl:variable name="errorViewCount"
							select="count(errors/transactionErrorOverviewCharts/chart)" />
						<xsl:variable name="errorsByMessageCount"
							select="count(errors/error[generate-id() = generate-id(key('errorsByMessage', message)[1])])" />
						<p>
							<xsl:if test="$errorViewCount &lt; $errorsByMessageCount">
								Displaying the first
								<b>
									<xsl:value-of select="$errorViewCount" />
								</b>
								overview charts for the error types that occurred most often.
							</xsl:if>
							<xsl:call-template name="show-n-hide">
								<xsl:with-param name="gid" select="$gid" />
							</xsl:call-template>
						</p>
						<div id="more-{$gid}" class="more">
							<p>
								The chart limit can be set or disabled by setting the
								corresponding report generator properties. (See
								<b>reportgenerator.properties</b>
								file for more details)
							</p>
						</div>
					</div>

					<xsl:choose>
						<xsl:when test="count(errors/transactionErrorOverviewCharts/chart) > 0">
							<div class="charts">
								<xsl:for-each select="errors/transactionErrorOverviewCharts/chart">
									<xsl:sort select="title" />
									<div class="chart">
										<div class="error">
											<img>
												<xsl:attribute name="id">
			                            			<xsl:value-of select="string(id)" />
			                            		</xsl:attribute>
												<xsl:attribute name="src">
				                            		<xsl:value-of
													select="concat('charts/errors/t',string(id),'.png')" />
				                            	</xsl:attribute>
												<xsl:attribute name="alt">Errors by Type</xsl:attribute>
											</img>
											<a class="backLink">
												<xsl:attribute name="href">
													<xsl:value-of select="concat('#tableEntry-',id)" />
												</xsl:attribute>
												Back to table
											</a>
										</div>
									</div>
								</xsl:for-each>
							</div>
						</xsl:when>
						<xsl:otherwise>
							<p class="text-center">
								There are no charts to show.
							</p>
						</xsl:otherwise>
					</xsl:choose>

					<!-- Transaction Error Details -->
					<h3 id="transaction-error-details">Transaction Error Details</h3>
					<div class="description">
						<xsl:variable name="gid"
							select="concat('transactionDetails', generate-id(.))" />
						<xsl:variable name="errorDetailsCount"
							select="count(errors/error[number(./detailChartID) != 0])" />
						<xsl:variable name="errorsCount" select="count(errors/error)" />
						<p>
							<xsl:if test="$errorDetailsCount &lt; $errorsCount">
								Displaying the first
								<b>
									<xsl:value-of select="$errorDetailsCount" />
								</b>
								detail charts for the error types that occurred most often,
                                grouped by test case/action.
							</xsl:if>
							<xsl:call-template name="show-n-hide">
								<xsl:with-param name="gid" select="$gid" />
							</xsl:call-template>
						</p>
						<div id="more-{$gid}" class="more">
							<p>
								The chart limit can be set or disabled by setting the
								corresponding report generator properties. (See
								<b>reportgenerator.properties</b>
								file for more details)
							</p>
						</div>
					</div>

					<table
						class="table-autosort:0 table-autosort-order:desc table-autostripe table-stripeclass:odd">
						<thead>
							<tr>
								<th class="table-sortable:numeric">Count</th>
								<th class="table-sortable:alphanumeric">Test Case</th>
								<th class="table-sortable:alphanumeric">Action</th>
								<th>Directory</th>
								<th class="table-sortable:alphanumeric">Error Information</th>
							</tr>
						</thead>
						<tfoot>
							<tr>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
						</tfoot>
						<xsl:choose>
							<xsl:when test="count($rootNode/error) > 0">
								<xsl:for-each select="$rootNode/error">
									<xsl:sort select="count" order="descending" data-type="number" />
									<xsl:sort select="detailChartID != 0" order="descending" />
									<tr>
										<td class="value number count">
											<xsl:value-of select="format-number(count, '#,##0')" />
										</td>
										<td class="value text testcasename">
											<xsl:value-of select="testCaseName" />
										</td>
										<td class="value text">
											<xsl:value-of select="actionName" />
										</td>
										<td class="value text directory">
											<xsl:for-each select="directoryHints/string">
												<xsl:choose>
													<xsl:when
														test="$rootNode/resultsPathPrefix and normalize-space(.)!='...'">
														<a>
															<xsl:attribute name="href"><xsl:value-of
																select="concat($rootNode/resultsPathPrefix, ., '/index.html')" /></xsl:attribute>
															<xsl:attribute name="target">_blank</xsl:attribute>
															<xsl:value-of select="." />
														</a>
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="." />
													</xsl:otherwise>
												</xsl:choose>
												<br />
											</xsl:for-each>
										</td>
										<!-- Error Details -->
										<td class="value text trace collapsible forcewordbreak">
											<div class="collapse">
												<xsl:value-of select="message" />
											</div>
											<pre>
												<xsl:value-of select="trace" />
											</pre>
											<!-- Error Details Charts -->
											<xsl:if test="string(detailChartID) != '0'">
												<div class="charts">
												    <div class="chart">
														<img>
															<xsl:attribute name="src">
	                                           					<xsl:value-of
																select="concat('charts/errors/d',string(detailChartID),'.png')"></xsl:value-of>
	                                           				</xsl:attribute>
															<xsl:attribute name="alt">Details Chart</xsl:attribute>
														</img>
    												</div>
												</div>
											</xsl:if>

											<xsl:variable name="errorMessage">
												<xsl:value-of select="message" />
											</xsl:variable>

											<xsl:if
												test="count($rootNode/transactionErrorOverviewCharts/chart) > 0">
												<xsl:variable name="overviewChartID">
													<xsl:value-of
														select="string($rootNode/transactionErrorOverviewCharts/chart[./title = $errorMessage]/id)" />
												</xsl:variable>

												<xsl:choose>
													<xsl:when test="string-length($overviewChartID) > 0">
														<a class="backlink" href="#overviewTable">
															<xsl:attribute name="href">
		                                       					<xsl:value-of
																select="concat('#',$overviewChartID)" />
		                                       				</xsl:attribute>
															Error overview chart
														</a>
													</xsl:when>
													<xsl:otherwise>
														<div>
															No error overview chart available.
														</div>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:if>
										</td>
									</tr>
								</xsl:for-each>
							</xsl:when>
							<xsl:otherwise>
								<tr>
									<td class="value text" colspan="5">There are no values to show
										in this table.</td>
								</tr>
							</xsl:otherwise>
						</xsl:choose>

					</table>
				</div>
			</div>
		</div>

	</xsl:template>

</xsl:stylesheet>
