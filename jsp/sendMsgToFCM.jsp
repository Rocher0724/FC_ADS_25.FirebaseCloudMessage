<%@ page language="java" contentType="text/html; char=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.security.*, java.io.*, java.net.*" %>
<%

String result = "메시지를 전송하였습니다.";

// 1. fcm 서버정보 세팅 
String fcm_url = "https://fcm.googleapis.com/fcm/send";
String content_type = "application/json";
String server_key = "AAAAtiwdICo:APA91bFjxgcFrcbEmmlIcfes6ON0X5qeYHIZerOTrnSWGRUyWgbKjjjKhepFcQtpZVxMYwqG3Aq4q7PfVKtFnXDQhgtDqJEoUu2yMgv4lnOrPRmJZJW7pipPIhLSE9NKLDVla9N_QV66";

// 2. 메시지 정보를 클라이언트(phone)로 부터 수신
request.setCharacterEncoding("UTF-8"); // 요청값이 한글일 경우 처리
String receiver_token 	= request.getParameter("receiver_token");
String msg				= request.getParameter("msg");
String sender			= request.getParameter("sender");
String title			= "보낸사람 : "+sender;
String point = "576434397";

// 3. fcm 서버로 메시지를 전송
// 3.1 수신한 메시지를 json 형태로 변경해준다.
// 노티를 구성하는 것을 서버에서 한다.
String json_string = "{\"to\": \"" + receiver_token + "\""
                   + ",\"data\" : { \"point\" : \"" + point + "\" }"
                   + ",\"notification\": { \"title\":\"" + title + "\""
                                        + ", \"body\": \"" + msg + "\""
                                        + ", \"click_action\" : \"PointActivity\"}"
                                        +"}";

// 3.2 HttpUrlConnection 을 사용해서 FCM 서버측으로 메시지를 전송한다.
// 		a. 서버연결
URL url = new URL(fcm_url);
HttpURLConnection con = (HttpURLConnection) url.openConnection();
//		b.header 설정
con.setRequestMethod("POST");
con.setRequestProperty("Authorization","key="+server_key);
con.setRequestProperty("content-Type",content_type);
//		c.POST데이터(body) 전송
con.setDoOutput(true);
OutputStream os = con.getOutputStream();
os.write(json_string.getBytes());
os.flush();
os.close();
//		d.전송후 결과처리 
int responseCode = con.getResponseCode();
if(responseCode == HttpURLConnection.HTTP_OK) { // code 200 이 HTTP_OK 의 코드이다.
	// 결과 처리 후 FCM 서버측에서 발송한 결과 메시지를 꺼낸다.
	BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
    // 4.2 반복문은 돌면서 버퍼의 데이터를 읽어온다.
    String dataLine = "";
    // 메시지를 한줄씩 읽어서 result변수에 담아두고 
    while( (dataLine = br.readLine()) != null ) {
    	result = result + dataLine;
	}
	br.close();
}


out.print("RESULT = " + result );

%>
