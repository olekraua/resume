package net.devstudy.resume.media.component;

import java.io.IOException;

import net.devstudy.resume.media.model.UploadTempPath;

public interface UploadTempPathFactory {

    UploadTempPath create() throws IOException;
}
