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
/**
 * 
 */
package com.xceptance.xlt.performance.util;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Session;

/**
 * This utility class is able to replace placeholders in Strings
 * with dynamic data. This class does not implement any error handling
 * for incorrect parameters data because of performance reason.
 * 
 * @author Rene Schwietzke
 */
public class ParameterUtils
{
	/**
	 * Replace all recognizable dynamic parameters in a String
	 */
	public static String replaceDynamicParameters(final String data)
	{
		String s = data;
		
		final int randomMatches = RegExUtils.getMatchingCount(s, "\\{random:[0-9]+\\}");
    	if (randomMatches > 0)
    	{
			final Pattern randomPattern = RegExUtils.getPattern("\\{random:([0-9]+)\\}");

    		for (int i = 0; i < randomMatches; i++)
    		{
    			final String count = RegExUtils.getFirstMatch(s, randomPattern, 1);
    			s = randomPattern.matcher(s).replaceFirst(RandomStringUtils.randomAlphabetic(Integer.valueOf(count)));
    		}
    	}

		if (s.contains("{userID}"))
    	{
    		s = s.replaceAll("\\{userID\\}", String.valueOf(Session.getCurrent().getUserID()));
    	}
		if (s.contains("{userNumber}"))
    	{
    		s = s.replaceAll("\\{userNumber\\}", String.valueOf(Session.getCurrent().getUserNumber()));
    	}
		
		return s;
	}
}
