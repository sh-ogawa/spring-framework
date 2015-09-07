/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.reactive.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.reactivestreams.Publisher;

import org.springframework.reactive.util.BlockingSignalQueue;

/**
 * {@code OutputStream} implementation that stores all written bytes, to be retrieved
 * using {@link #toByteArrayPublisher()}.
 * @author Arjen Poutsma
 */
public class ByteArrayPublisherOutputStream extends OutputStream {

	private final BlockingSignalQueue<byte[]> queue = new BlockingSignalQueue<>();

	/**
	 * Returns the written data as a {@code Publisher}.
	 * @return a publisher for the written bytes
	 */
	public Publisher<byte[]> toByteArrayPublisher() {
		return this.queue.publisher();
	}

	@Override
	public void write(int b) throws IOException {
		write(new byte[]{(byte) b});
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] copy = Arrays.copyOf(b, len);
		try {
			this.queue.putSignal(copy);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.queue.complete();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
