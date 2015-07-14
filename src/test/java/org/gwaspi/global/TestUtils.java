/*
 * Copyright (C) 2013 Universitat Pompeu Fabra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gwaspi.global;

import org.junit.Assert;
import org.junit.Test;

public class TestUtils {

	@Test
	public void testToHumanReadableNum() {

		Assert.assertEquals("-1", Utils.toHumanReadableNum(-1));
		Assert.assertEquals("-123", Utils.toHumanReadableNum(-123));
		Assert.assertEquals("-123'456", Utils.toHumanReadableNum(-123456));
		Assert.assertEquals("-1'234'567'234", Utils.toHumanReadableNum(-1234567234));

		Assert.assertEquals("1", Utils.toHumanReadableNum(1));
		Assert.assertEquals("123", Utils.toHumanReadableNum(123));
		Assert.assertEquals("123'456", Utils.toHumanReadableNum(123456));
		Assert.assertEquals("1'234'567'234", Utils.toHumanReadableNum(1234567234));
	}
}
