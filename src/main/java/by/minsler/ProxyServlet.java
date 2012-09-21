package by.minsler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import by.minsler.boundary.BoundaryGenerator;

public class ProxyServlet extends MainServlet {

	private static Logger logger = Logger.getLogger(ProxyServlet.class);

	URL receiverUrl = null;

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String url = config.getInitParameter("receiverUrl");
		try {
			receiverUrl = new URL(url);
			logger.info("url initialized");
		} catch (MalformedURLException e) {
			logger.error("error initializing url" + e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("Connected to servlet");
		resp.setContentType("text/html;charset=utf-8");
		resp.setCharacterEncoding("utf-8");
		req.setCharacterEncoding("utf-8");

		PrintWriter out = resp.getWriter();
		String contentType = req.getContentType();
		DataInputStream dis = new DataInputStream(req.getInputStream());

		int contentLength = req.getContentLength();
		String boundaryOfInputRequest = contentType.substring(contentType
				.indexOf("boundary=") + 9);

		// set buffer size fo stream
		byte buffer[] = new byte[8 * 1024];

		boolean isBodyAttachment = false;
		StringBuilder sb = new StringBuilder();
		int nread;

		String contentIdPayload = "Content-ID: Payload-0" + "\n\n";
		int indexFrom = 0;
		int indexContentIdPayload = -1;

		// search index of string "Content-ID: Payload-0\n\n"
		while ((nread = dis.read(buffer)) >= 0) {
			String substring = new String(buffer, 0, nread);
			sb.append(substring);
			if ((indexContentIdPayload = sb
					.indexOf(contentIdPayload, indexFrom)) >= 0) {
				isBodyAttachment = true;
				break;
			} else {
				indexFrom = sb.length() - contentIdPayload.length() - 1;
			}
		}

		String endBoundary = "--" + boundaryOfInputRequest + "--" + "\n";

		// calculate index of byte start attachment
		int endIndex = indexContentIdPayload + contentIdPayload.length();

		// search start and end index for soap xml part of request
		int startSoapXml = sb.indexOf("Content-Id: <soappart>\n\n")
				+ "Content-Id: <soappart>\n\n".length();
		int endSaopXml = sb.indexOf("\n--" + boundaryOfInputRequest);

		// create file for save soap part
		String homeDir = getServletContext().getRealPath("/");
		File soapFile = new File(homeDir, "WEB-INF/upload/uploaded-soap.xml");
		FileOutputStream fosSoapFile = new FileOutputStream(soapFile);

		// save to file soap part
		fosSoapFile.write(sb.substring(startSoapXml, endSaopXml).getBytes());
		fosSoapFile.flush();
		fosSoapFile.close();

		// calculate lenth of attachemnt part from input request
		int availableBytesForWrite = contentLength
				- sb.substring(0, endIndex).getBytes().length
				- endBoundary.getBytes().length;

		HttpURLConnection connection = (HttpURLConnection) receiverUrl
				.openConnection();

		connection.setDoOutput(true);
		connection.setRequestMethod("POST");

		// create boundary for new request to end receiver
		String boundaryForNewRequest = BoundaryGenerator.generateBoundary();

		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundaryForNewRequest);

		String attachmentHeaders = "--" + boundaryForNewRequest + "\n"
				+ "Content-Type: text/plain; charset=\"utf-8\"\n"
				+ "Content-ID: Payload-0\n\n";
		String multipartEnd = "--" + boundaryForNewRequest + "--\n";

		StringBuilder sbNewRequest = new StringBuilder();
		sbNewRequest.append(attachmentHeaders).append(multipartEnd);

		// calculate length of content
		long lentghOfStream = availableBytesForWrite
				+ new String(sbNewRequest).getBytes().length;

		// set content length for request
		connection.setFixedLengthStreamingMode(lentghOfStream);

		DataOutputStream dos = new DataOutputStream(
				connection.getOutputStream());

		dos.writeBytes(attachmentHeaders);

		// start write bytes to output stream of post request
		availableBytesForWrite -= super.writeToOutput(dos,
				sb.substring(endIndex).getBytes(), availableBytesForWrite);

		while ((nread = dis.read(buffer)) >= 0) {
			availableBytesForWrite -= super.writeToOutput(dos, buffer, 0,
					nread, availableBytesForWrite);

		}

		dos.writeBytes(multipartEnd);
		dos.flush();
		dos.close();
		logger.info("response code from end receiver url: "
				+ connection.getResponseCode());
		connection.disconnect();
		out.println("success");
	}
}
