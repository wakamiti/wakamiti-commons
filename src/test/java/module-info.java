/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
module wakamiti.commons.test {

    requires org.junit.jupiter.params;
    requires wakamiti.commons;

    exports es.wakamiti.commons.test.lang;
    exports es.wakamiti.commons.test.security;

    opens es.wakamiti.commons.test.lang to org.junit.platform.commons;
    opens es.wakamiti.commons.test.security to org.junit.platform.commons;

}