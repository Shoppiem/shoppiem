package com.shoppiem.api.service.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.shoppiem.api.dto.Pair;
import com.shoppiem.api.props.AWSProps;
import com.shoppiem.api.service.utils.AsyncTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Bizuwork Melesse
 * created on 2/13/22
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {
    private final AmazonS3 s3Client;
    private final AWSProps awsProps;

    @Override
    public void uploadImage(String bucket, String key, InputStream stream, String contentType) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(stream.available());
            meta.setContentType(contentType);
            s3Client.putObject(new PutObjectRequest(
                    bucket, key, stream, meta)
                    .withCannedAcl(CannedAccessControlList.Private));
            log.info("S3ServiceImpl.uploadContent Image uploaded to S3: {}", getCdnUrl(key));
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCdnUrl(String key) {
        return awsProps.getCdn() + "/" + key;
    }

    @Override
    public void uploadImagesAsync(String bucket, List<Pair<String, String>> fileNameKeyList, String contentType) {
        for (Pair<String, String> pair : fileNameKeyList) {
            AsyncTask.submit(() -> {
                    try (InputStream in = new FileInputStream(pair.getFirst())) {
                        uploadImage(bucket, pair.getSecond(), in, contentType);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                null);
        }
    }

    @Override
    public void uploadImagesSync(String bucket, List<Pair<String, String>> fileNameKeyList,
        String contentType) {
        for (Pair<String, String> pair : fileNameKeyList) {
            try (InputStream in = new FileInputStream(pair.getFirst())) {
                uploadImage(bucket, pair.getSecond(), in, contentType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void uploadHlsData(String bucket, String contentId, String path, String folderPrefix) {
        File hlsFile = new File(path);
        if (hlsFile.exists()) {
            File f = new File(hlsFile.getParent());
            String[] pathnames = f.list();
            List<String> hlsFiles = getSortedHlsFiles(pathnames, contentId, f.getPath());

            String folder = folderPrefix + "/" + contentId + "/";
            log.info("Uploading {} HLS files to S3 for mediaId {}", hlsFile.length(), contentId);
            for (String hls : hlsFiles) {
                AsyncTask.submit(() -> uploadTask(hls, folder, bucket),
                    () -> cleanup(List.of(hls))
                );
            }
        } else {
            log.warn("S3ServiceImpl.uploadHlsData HLS file {} does not exist. Aborting upload task",
                path);
        }
    }

    private List<String> getSortedHlsFiles(String[] pathnames, String contentId, String parentPath) {
        // Sort the HLS files so that the index file gets uploaded first and the
        // actual content gets uploaded in order. That way, a user may start streaming
        // the content before the uploading is finished.
        String indexFile = "";
        List<String> files = new ArrayList<>();
        if (pathnames != null) {
            for (String p : pathnames) {
                p = parentPath + '/' + p;
                if (p.contains(contentId)) {
                    if (p.contains(".ts")) {
                        files.add(p);
                    } else if (p.contains(".m3u8")) {
                        indexFile = p;
                    }
                }
            }
        }
        // Sort the file names so that the numerical suffixes also
        // get sorted in order
        files.sort(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });
        files.add(0, indexFile);
        return files;
    }

    private Void uploadTask(String hlsFile, String folder, String bucket) {
        File file = new File(hlsFile);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(in.available());
                meta.setContentType("application/octet-stream");
                String key = folder + file.getName();
                s3Client.putObject(new PutObjectRequest(
                    bucket, key, in, meta)
                    .withCannedAcl(CannedAccessControlList.Private));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Void cleanup(List<String> toDelete) {
        for (String path : toDelete) {
            File file = new File(path);
            file.delete();
        }
        return null;
    }

    @Override
    public String getObject(String bucket, String key) {
        try {
            S3Object o = s3Client.getObject(bucket, key);
            S3ObjectInputStream s3is = o.getObjectContent();
            return new String(s3is.readAllBytes());
        } catch (IOException | AmazonServiceException e) {
            log.error("Error while fetching S3 object with key {}: {}", key, e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    public CopyObjectResult copyObject(String srcBucket, String srcKey, String dstBucket, String dstKey) {
        return s3Client.copyObject(srcBucket, srcKey, dstBucket, dstKey);
    }

    @Override
    public void deleteObject(String bucket, String key) {
        s3Client.deleteObject(bucket, key);
    }
}


