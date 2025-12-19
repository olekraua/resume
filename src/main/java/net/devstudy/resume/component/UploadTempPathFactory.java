package net.devstudy.resume.component;

import java.io.IOException;

import net.devstudy.resume.model.UploadTempPath;

public interface UploadTempPathFactory {

    UploadTempPath create() throws IOException;
}
