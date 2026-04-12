/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine;

import java.util.List;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.common.util.CsvByteRow;
import com.xceptance.common.util.ByteCsvDecoder;

public class CsvByteRowTestHelper {
    public static CsvByteRow toByteRow(List<XltCharBuffer> buffers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffers.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(buffers.get(i).toString());
        }
        byte[] bytes = sb.toString().getBytes();
        return ByteCsvDecoder.parse(bytes, 0, bytes.length);
    }
}
