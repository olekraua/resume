package net.devstudy.resume.component.impl;

import java.io.IOException;

import org.springframework.stereotype.Component;

import net.devstudy.resume.component.UploadTempPathFactory;
import net.devstudy.resume.model.UploadTempPath;

@Component
public class DefaultUploadTempPathFactory implements UploadTempPathFactory {

    @Override
    public UploadTempPath create() throws IOException {
        return new UploadTempPath();
    }
}
