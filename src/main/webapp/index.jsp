<!DOCTYPE html>
<head>
	<title>New page for test proxy</title>
	<meta charset="utf-8">
</head>

<body>
<h2>Hello World!</h2>
<h2>Servlet with allocation file in memory</h2>
<form action="proxyInMemory" method="post" enctype="multipart/form-data" >
<input type="file" name="data">
<input type="submit" value="send file" >
</form>

<hr>
<h2>Servlet with Stream to disk</h2>
<form action="proxyToDisk" method="post" enctype="multipart/form-data" >
<input type="file" name="data">
<input type="submit" value="send file" >
</form>
<hr>
</body>
</html>