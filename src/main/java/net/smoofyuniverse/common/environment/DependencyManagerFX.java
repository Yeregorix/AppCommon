/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.common.environment;

import net.smoofyuniverse.common.app.ApplicationManager;
import net.smoofyuniverse.common.app.Translations;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.task.IncrementalListener;
import net.smoofyuniverse.common.task.ProgressTask;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class DependencyManagerFX extends DependencyManager {

	public DependencyManagerFX(ApplicationManager app, Collection<DependencyInfo> dependencies) {
		super(app, dependencies);
	}

	@Override
	protected void download(List<DependencyInfo> deps, long totalSize) {
		Consumer<ProgressTask> consumer = task -> {
			logger.info("Downloading missing dependencies ...");
			task.setTitle(Translations.dependencies_download_title);
			IncrementalListener listener = task.expect(totalSize);

			Iterator<DependencyInfo> it = deps.iterator();
			while (it.hasNext()) {
				if (task.isCancelled())
					return;

				DependencyInfo dep = it.next();
				logger.info("Downloading dependency {} ...", dep.name);
				task.setMessage(dep.name);

				if (!dep.createParent() || !dep.download(this.app.getConnectionConfig(), listener))
					continue;

				if (task.isCancelled())
					return;

				if (dep.matches())
					it.remove();
				else
					logger.warn("The downloaded dependency has an incorrect signature.");
			}
		};

		Popup.consumer(consumer).title(Translations.dependencies_update_title).submitAndWait();
	}

	@Override
	protected void failed(List<DependencyInfo> deps) {
		StringBuilder b = new StringBuilder();
		for (DependencyInfo dep : deps)
			b.append("\n- ").append(dep.name);

		Popup.error().title(Translations.failed_dependencies_title).message(Translations.failed_dependencies_message.format("list", b.toString())).showAndWait();
	}
}
