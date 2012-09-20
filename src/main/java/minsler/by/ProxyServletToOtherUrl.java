package minsler.by;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServletToOtherUrl extends HttpServlet {

	URL receiverUrl = null;

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String url = config.getInitParameter("receiverUrl");
		try {
			receiverUrl = new URL(url);
			System.out.println("url initialized");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error initialized url");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("ProxyServlet: connected to servlet");
		String message = "Файл загружен успешно!";
		String err = "Ошибка при чтении файла - файл неопределен";
		resp.setContentType("text/html;charset=utf-8");

		PrintWriter out = resp.getWriter();
		String contentType = req.getContentType();
		DataInputStream dis = new DataInputStream(req.getInputStream());
		// if is multipart then

		int length = req.getContentLength();

		HttpURLConnection connection = (HttpURLConnection) receiverUrl
				.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		// TO-DO boundary
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=boundary.minsler.by");
		connection.setFixedLengthStreamingMode(length);
		DataOutputStream dos = new DataOutputStream(
				connection.getOutputStream());

		byte array[] = new byte[8 * 1024];
		int nread;

		while ((nread = dis.read(array)) >= 0) {
			System.out.println("ProxyServlet: nread: " + nread);
			dos.write(array, 0, nread);
		}

		dos.flush();
		dos.close();
		System.out.println("ProxyServlet: response code from toDisk servlet"
				+ connection.getResponseCode());
		connection.disconnect();

		out.println("<html>" + "<head>"
				+ "<title>Загрузка файла на сервер</title>" + "</head>"
				+ "<body bgcolor=#fff1df>" + "<h3>" + message + "</h3>"
				+ "</body>" + "</html>");
		out.close();
	}
}
