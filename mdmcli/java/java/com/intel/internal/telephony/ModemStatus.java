/*
 * Copyright (C) Intel 2012
 *
 * Modem Client library has been designed by:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
 *  - Marc Bellanger <marc.bellanger@intel.com>
 *
 * Original contributors are:
 *  - Cesar De Oliveira <cesar.de.oliveira@intel.com>
 *  - Edward Marmounier <edward.marmounier@intel.com>
 *  - Lionel Ulmer <lionel.ulmer@intel.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.internal.telephony;

/* @TODO: Delete this enum. Kept currently for backward compatibility
 * enums must be avoided.
 */

public enum ModemStatus {
    NONE(0),
    ALL(0), // DEPRECATED: remove this
    DOWN(1),
    UP(2),
    DEAD(3);

    private int value;

    private ModemStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
