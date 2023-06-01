/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.external.reportObject;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author matthias.ullrich
 */
@XStreamAlias("genericReport")
public class GenericReport
{
    @XStreamAlias("headline")
    public String headline;

    @XStreamAlias("description")
    public String description;

    @XStreamAlias("tables")
    public final List<Table> tables = new ArrayList<Table>();

    @XStreamAlias("chartFileNames")
    public final List<String> chartFileNames = new ArrayList<String>();
}
