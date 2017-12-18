package net.bleujin.rcraken.script;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

public abstract class AbstractListener implements FileAlterationListener{
	@Override
	public void onStop(FileAlterationObserver filealterationobserver) {
	}
	
	@Override
	public void onStart(FileAlterationObserver filealterationobserver) {
	}
	
	@Override
	public void onFileDelete(File file) {
	}
	
	@Override
	public void onFileCreate(File file) {
	}

	@Override
	public void onDirectoryDelete(File file) {
	}
	
	@Override
	public void onDirectoryCreate(File file) {
	}
	
	@Override
	public void onDirectoryChange(File file) {
	}
}
