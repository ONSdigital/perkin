package com.github.onsdigital.perkin.publish;

import com.github.onsdigital.Configuration;
import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.transform.DataFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class FtpPublisherTest {

    @Mock
    private DataFile file;
    private List<DataFile> files;

    private FtpPublisher classUnderTest;

    private FakeFtpServer fakeFtpServer;
    private int port;

    @Before
    public void setUp() throws IOException {

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(0);  // use any free port
        fakeFtpServer.addUserAccount(new UserAccount("ons", "ons", "/"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();
        port = fakeFtpServer.getServerControlPort();

        //create publisher
        Configuration.set("FTP_HOST", "localhost");
        Configuration.set("FTP_PORT", port);
        ConfigurationManager.getInstance().loadConfiguration();
        classUnderTest = new FtpPublisher();

        //mock files to publish
        when(file.getFilename()).thenReturn("test.txt");
        when(file.getBytes()).thenReturn("test.txt contents".getBytes(StandardCharsets.UTF_8));
        files = new ArrayList<>();
        files.add(file);

        log.debug("TEST|FakeFtpServer running on port " + port);
    }

    @After
    public void tearDown() {
        fakeFtpServer.stop();
    }

    @Test(expected = IOException.class)
    public void shouldErrorConnectionRefused() throws IOException {
        //given
        Configuration.set("FTP_PORT", 8888);
        ConfigurationManager.getInstance().loadConfiguration();
        classUnderTest = new FtpPublisher();

        //when
        classUnderTest.publish(files);
    }

    @Test
    public void shouldLogoutOnTransferError() throws IOException {
        DataFile fileToFail = mock(DataFile.class);

        // Create some content that will fail
        when(fileToFail.getFilename()).thenReturn("test&*aslkd  //2.txt");
        when(fileToFail.getBytes()).thenReturn(new byte[0]);

        files.add(fileToFail);

        classUnderTest.publish(files);

        assertThat(classUnderTest.isLoggedIn(), is(false));
    }

    @Test(expected = IOException.class)
    public void shouldErrorInvalidCredentials() throws IOException {
        //given
        Configuration.set("FTP_USER", "invalid");
        ConfigurationManager.getInstance().loadConfiguration();
        classUnderTest = new FtpPublisher();

        //when
        classUnderTest.publish(files);
    }

    @Test
    public void shouldPublishFile() throws IOException {
        //given

        //when
        classUnderTest.publish(files);

        //then
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect("localhost", port);
        ftpClient.login("ons", "ons");
        FTPFile[] files = ftpClient.listFiles();

        for (FTPFile file : files) {
            log.debug("TEST|FtpPublisherTest - ftp list. file: " + file.getName());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ftpClient.retrieveFile(files[0].getName(), out);
        String contents = out.toString("UTF-8");
        log.debug("TEST|test.txt: {}", contents);
        ftpClient.quit();
        ftpClient.disconnect();

        assertThat(files.length, is(1));
        assertThat(files[0].getName(), is("test.txt"));
    }

    @Test
    public void shouldDeterminePathReceipts() {
        //given
        String path = "\\\\NP3RVWAPXX370\\SDX_PROD\\EDC_QReceipts\\";

        //when
        String determined = classUnderTest.determinePath(path);

        //then
        assertThat(determined, is("EDC_QReceipts"));
    }

    @Test
    public void shouldDeterminePathPck() {
        //given
        String path = "\\\\NP3RVWAPXX370\\SDX_PROD\\EDC_QData\\";

        //when
        String determined = classUnderTest.determinePath(path);

        //then
        assertThat(determined, is("EDC_QData"));
    }

    @Test
    public void shouldDeterminePathImages() {
        //given
        String path = "\\\\NP3RVWAPXX370\\SDX_PROD\\EDC_QImages\\Images\\";

        //when
        String determined = classUnderTest.determinePath(path);

        //then
        assertThat(determined, is("EDC_QImages/Images"));
    }

    @Test
    public void shouldDeterminePathIndex() {
        //given
        String path = "\\\\NP3RVWAPXX370\\SDX_PROD\\EDC_QImages\\Index\\";

        //when
        String determined = classUnderTest.determinePath(path);

        //then
        assertThat(determined, is("EDC_QImages/Index"));
    }
}
