package io.github.yangziwen.zyftp.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.apache.commons.lang3.math.NumberUtils;

import io.github.yangziwen.zyftp.command.impl.state.CommandState;
import io.github.yangziwen.zyftp.command.impl.state.StorState;
import io.github.yangziwen.zyftp.filesystem.FileView;
import lombok.Data;

@Data
public class UploadFileInfo {

	private long offset;

	private RandomAccessFile file;

	private long receivedTotalBytes;

	private FtpSession session;

	public UploadFileInfo(FtpSession session) {
		this.session = session;
		this.offset = -1;
		this.file = null;
		CommandState state = session.getCommandState();
		if (!StorState.class.isInstance(state)) {
			return;
		}
		FtpRequest restRequest = state.getRequest("REST");
		this.offset = restRequest == null ? 0 : NumberUtils.toLong(restRequest.getArgument());
		this.file = getUploadFile(session, state);
	}

	public boolean isValid() {
		return offset >= 0 && file != null;
	}

	public long getAndAddOffset(long delta) {
		long offset = this.offset;
		this.offset += delta;
		return offset;
	}

	private RandomAccessFile getUploadFile(FtpSession session, CommandState commandState) {
    	String fileName = commandState.getRequest("STOR").getArgument();
    	FileView fileView = session.getFileSystemView().getFile(fileName);
    	if (fileView == null) {
    		return null;
    	}
    	try {
			return new RandomAccessFile(fileView.getRealFile(), "rw");
		} catch (Exception e) {
			return null;
		}
    }

	public FileChannel getFileChannel() {
		return file != null ? file.getChannel() : null;
	}

	public void close() {
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
