/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.common.environment.source;

import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.environment.ReleaseInfo;

import java.util.Optional;

/**
 * An index of releases.
 */
public interface ReleaseSource {

	/**
	 * Gets the latest version.
	 *
	 * @return The latest version.
	 */
	Optional<String> getLatestVersion();

	/**
	 * Gets the release of the current {@link Application} version.
	 *
	 * @return The current release.
	 */
	default Optional<ReleaseInfo> getCurrentRelease() {
		return getRelease(Application.get().getVersion());
	}

	/**
	 * Gets the release of the given version.
	 *
	 * @param version The version.
	 * @return The release.
	 */
	Optional<ReleaseInfo> getRelease(String version);

	/**
	 * Gets the latest release.
	 *
	 * @return The latest release.
	 */
	Optional<ReleaseInfo> getLatestRelease();
}
