/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.lang;

public class ParseBoolean 
{
	/**
	 * Parses chars and evaluates if this is a boolean. Anything that is not true or TRUE
	 * or similar to True will evaluate to false. This is optimized for speed.
	 * 
	 * @param c a sequence with characters, null is not permitted
	 * @return true when chars match case-insensitive, false in any other case
	 */
	public static boolean parse(final CharSequence c)
	{
		// length is incorrect, it must be false
		if (c.length() != 4)
		{
			return false;
		}
		
		// it is length 4, safe here
		final char t = c.charAt(0);
		final char r = c.charAt(1);
		final char u = c.charAt(2);
		final char e = c.charAt(3);
		
		// fastpath
		final boolean b1 = (t == 't' & r == 'r' & u == 'u' & e == 'e');
		
		// slowpath will only be taken when needed, expected most true/false char buffers to be lowercase only
		return b1 ? true : ((t == 't' || t == 'T') && (r == 'r' || r == 'R') && (u == 'u' || u == 'U') && (e == 'e' || e == 'E'));
	}
}
