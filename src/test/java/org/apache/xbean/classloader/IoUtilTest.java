package org.apache.xbean.classloader;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.jar.JarFile;

import junit.framework.TestCase;

import org.apache.xbean.classloader.IoUtil.EmptyInputStream;

import sun.misc.IOUtils;

/**
 * Test the {@link IOUtils}.
 * 
 * @author Phillip Webb
 */
public class IoUtilTest extends TestCase {

	/**
	 * Given {@link IoUtil} when getBytes() is called then the data should be read from the stream and the stream should
	 * be closed.
	 * 
	 * @throws Exception
	 */
	public void testGetBytesReturnsValidDataAndClosesStream() throws Exception {
		byte[] bytes = new byte[8000];
		new Random().nextBytes(bytes);
		ByteArrayInputStreamSpy inputStream = new ByteArrayInputStreamSpy(bytes);
		assertTrue(Arrays.equals(bytes, IoUtil.getBytes(inputStream)));
		assertTrue(inputStream.isClosed());
	}

	/**
	 * Given {@link IoUtil} when flush(OutputStream) is called then flush is called on the underlying stream.
	 * 
	 * @throws Exception
	 */
	public void testFlushOutputStream() throws Exception {
		OutputStream outputStream = mock(OutputStream.class);
		IoUtil.flush(outputStream);
		verify(outputStream).flush();
	}

	/**
	 * Given {@link IoUtil} when flush(OutputStream) is called and an IOException is raised then the exception is
	 * swallowed.
	 * 
	 * @throws Exception
	 */
	public void testFlushOutputStreamWithException() throws Exception {
		OutputStream outputStream = mock(OutputStream.class);
		doThrow(new IOException()).when(outputStream).flush();
		IoUtil.flush(outputStream);
	}

	/**
	 * Given {@link IoUtil} when flush(null) is called nothing happens.
	 * 
	 * @throws Exception
	 */
	public void testFlushOutputStreamWithNull() throws Exception {
		IoUtil.flush(null);
	}

	/**
	 * Given {@link IoUtil} when close(JarFile) is called then close is called on the underlying jar.
	 * 
	 * @throws Exception
	 */
	public void testCloseJarFile() throws Exception {
		JarFile jarFile = mock(JarFile.class);
		IoUtil.close(jarFile);
		verify(jarFile).close();
	}

	/**
	 * Given {@link IoUtil} when close(jarFile) is called and an Exception is raised then the exception is swallowed.
	 * 
	 * @throws Exception
	 */
	public void testCloseJarFileWithException() throws Exception {
		JarFile jarFile = mock(JarFile.class);
		doThrow(new IOException()).when(jarFile).close();
		IoUtil.close(jarFile);
	}

	/**
	 * Given {@link IoUtil} when close(null) is called nothing happens.
	 * 
	 * @throws Exception
	 */
	public void testCloseJarFileWithNull() throws Exception {
		IoUtil.close((JarFile) null);
	}

	/**
	 * Given {@link IoUtil} when close(InputStream) is called then close is called on the underlying stream.
	 * 
	 * @throws Exception
	 */
	public void testCloseInputStream() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		IoUtil.close(inputStream);
		verify(inputStream).close();
	}

	/**
	 * Given {@link IoUtil} when close(InputStream) is called and an Exception is raised then the exception is
	 * swallowed.
	 * 
	 * @throws Exception
	 */
	public void testCloseInputStreamWithException() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		doThrow(new IOException()).when(inputStream).close();
		IoUtil.close(inputStream);
	}

	/**
	 * Given {@link IoUtil} when close(null) is called nothing happens.
	 * 
	 * @throws Exception
	 */
	public void testCloseInputStreamWithNull() throws Exception {
		IoUtil.close((InputStream) null);
	}

	/**
	 * Given {@link IoUtil} when close(OutputStream) is called then close is called on the underlying stream.
	 * 
	 * @throws Exception
	 */
	public void testCloseOutputStream() throws Exception {
		OutputStream outputStream = mock(OutputStream.class);
		IoUtil.close(outputStream);
		verify(outputStream).close();
	}

	/**
	 * Given {@link IoUtil} when close(OutputStream) is called and an Exception is raised then the exception is
	 * swallowed.
	 * 
	 * @throws Exception
	 */
	public void testCloseOutputStreamWithException() throws Exception {
		OutputStream outputStream = mock(OutputStream.class);
		doThrow(new IOException()).when(outputStream).close();
		IoUtil.close(outputStream);
	}

	/**
	 * Given {@link IoUtil} when close(null) is called nothing happens.
	 * 
	 * @throws Exception
	 */
	public void testCloseOutputStreamWithNull() throws Exception {
		IoUtil.close((OutputStream) null);
	}

	/**
	 * Given a {@link EmptyInputStream} when any read method is called then no data is read.
	 * 
	 * @throws Exception
	 */
	public void testEmptyInputStream() throws Exception {
		EmptyInputStream emptyInputStream = new EmptyInputStream();
		assertEquals(-1, emptyInputStream.read());
		assertEquals(-1, emptyInputStream.read(new byte[] { 0, 1 }, 0, 2));
		assertEquals(0, emptyInputStream.skip(100));
	}

	private static class ByteArrayInputStreamSpy extends ByteArrayInputStream {
		private boolean closed;

		public ByteArrayInputStreamSpy(byte[] buf) {
			super(buf);
		}

		public void close() throws IOException {
			super.close();
			closed = true;
		}

		public boolean isClosed() {
			return closed;
		}
	}
}
