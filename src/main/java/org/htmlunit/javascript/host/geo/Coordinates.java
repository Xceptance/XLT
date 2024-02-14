/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript.host.geo;

import static org.htmlunit.javascript.configuration.SupportedBrowser.IE;

import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.configuration.JsxClass;
import org.htmlunit.javascript.configuration.JsxGetter;

/**
 * A JavaScript object for {@code Coordinates}.
 *
 * @author Ahmed Ashour
 */
@JsxClass(IE)
public class Coordinates extends HtmlUnitScriptable {

    private double latitude_;
    private double longitude_;
    private double accuracy_;

    /**
     * Creates an instance.
     */
    public Coordinates() {
    }

    Coordinates(final double latitude, final double longitude, final double accuracy) {
        latitude_ = latitude;
        longitude_ = longitude;
        accuracy_ = accuracy;
    }

    /**
     * Returns the latitude.
     * @return the latitude
     */
    @JsxGetter
    public double getLatitude() {
        return latitude_;
    }

    /**
     * Returns the longitude.
     * @return the longitude
     */
    @JsxGetter
    public double getLongitude() {
        return longitude_;
    }

    /**
     * Returns the accuracy.
     * @return the accuracy
     */
    @JsxGetter
    public double getAccuracy() {
        return accuracy_;
    }

}
