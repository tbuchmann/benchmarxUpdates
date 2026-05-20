package org.benchmarx.dsl.generator;

import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccess2;

/**
 * Wraps an {@link IFileSystemAccess2} and skips generation for files that
 * already exist (used for hand-edited stubs that must not be overwritten).
 */
public class SkipExistingFsa implements IFileSystemAccess2 {

	private final IFileSystemAccess2 delegate;

	public SkipExistingFsa(IFileSystemAccess2 delegate) {
		this.delegate = delegate;
	}

	@Override
	public void generateFile(String fileName, CharSequence contents) {
		if (delegate.isFile(fileName)) return;
		delegate.generateFile(fileName, contents);
	}

	@Override
	public void generateFile(String fileName, String outputCfgName, CharSequence contents) {
		if (delegate.isFile(fileName, outputCfgName)) return;
		delegate.generateFile(fileName, outputCfgName, contents);
	}

	@Override public void deleteFile(String fileName) { delegate.deleteFile(fileName); }
	@Override public void deleteFile(String fileName, String outputCfgName) { delegate.deleteFile(fileName, outputCfgName); }
	@Override public CharSequence readTextFile(String fileName) { return delegate.readTextFile(fileName); }
	@Override public CharSequence readTextFile(String fileName, String outputCfgName) { return delegate.readTextFile(fileName, outputCfgName); }
	@Override public boolean isFile(String path) { return delegate.isFile(path); }
	@Override public boolean isFile(String path, String outputCfgName) { return delegate.isFile(path, outputCfgName); }
	@Override public URI getURI(String path) { return delegate.getURI(path); }
	@Override public URI getURI(String path, String outputCfgName) { return delegate.getURI(path, outputCfgName); }
	@Override public void generateFile(String fileName, InputStream content) { delegate.generateFile(fileName, content); }
	@Override public void generateFile(String fileName, String outputCfgName, InputStream content) { delegate.generateFile(fileName, outputCfgName, content); }
	@Override public InputStream readBinaryFile(String fileName) { return delegate.readBinaryFile(fileName); }
	@Override public InputStream readBinaryFile(String fileName, String outputCfgName) { return delegate.readBinaryFile(fileName, outputCfgName); }
}
