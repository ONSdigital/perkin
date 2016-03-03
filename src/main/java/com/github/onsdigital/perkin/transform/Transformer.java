package com.github.onsdigital.perkin.transform;

import com.github.davidcarboni.httpino.Response;
import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.decrypt.HttpDecrypt;
import com.github.onsdigital.perkin.json.IdbrReceipt;
import com.github.onsdigital.perkin.json.Result;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.publish.HttpPublisher;
import org.apache.http.StatusLine;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transform a Survey into a format for downstream systems.
 */
public class Transformer {

    //TODO: service/api for batchId
    private static AtomicLong batchId = new AtomicLong(35000);

    private HttpPublisher publisher = new HttpPublisher();
    private HttpDecrypt decrypt = new HttpDecrypt();
    private IdbrReceiptBuilder idbrReceiptFactory = new IdbrReceiptBuilder();

    public Response<Result> transform(final String data) throws IOException {

        System.out.println("transform data " + data);

        Response<Survey> decryptResponse = decrypt.decrypt(data);
        System.out.println("decrypt <<<<<<<< response: " + Json.format(decryptResponse));

        if (isError(decryptResponse.statusLine)) {
            return new Response<>(decryptResponse.statusLine, Result.builder().error(true).message("problem decrypting").build());
        }

        Survey survey = decryptResponse.body;
        IdbrReceipt receipt = idbrReceiptFactory.createIdbrReceipt(survey, batchId.getAndIncrement());
        System.out.println("transform created IDBR receipt: " + Json.format(receipt));

        Response<Result> result = publisher.publish(receipt);
        System.out.println("transform <<<<<<<< response: " + Json.format(result));
        System.out.println("transform <<<<<<<< response: result.body.isError() " + result.body.isError());
        System.out.println("transform <<<<<<<< response: result.statusLine.getStatusCode() " + result.statusLine.getStatusCode());

        return result;
    }

    private boolean isError(StatusLine statusLine) {
        return statusLine.getStatusCode() != HttpStatus.OK_200;
    }
}
