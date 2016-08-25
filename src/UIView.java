public class UIView
{
	public static String viewLogin(String loginMessage)
	{

		String myScript = getJavaScriptFunctions();
		String onLoad = "login()";
		String msg = OutsourcerView.getHead(myScript, onLoad);
		msg += "<table class=\"ostable\">\n";
		msg += "<tr><td><h1><a href=\"/\">Oracle2GP</a></h1></td>\n";
		msg += "</tr>\n";
		msg += "</table>\n";
		msg += "<form action=\"/\" name=\"login\" id=\"login\" method=\"post\">\n";
		msg += "<input type=\"hidden\" id=\"submit_form\" name=\"submit_form\" value=\"1\">\n";
		msg += "<table class=\"logintable\" width=\"100%\">\n";
		msg += "<tr><td align=\"center\">\n";
		msg += "<table class=\"logintable\" style=\"height: 375px;width: 851px;\">\n";
        msg += "<tbody style=\"background-color: #EE7600;\">\n";
		msg += "<tr>\n";
		msg += "<td style=\"padding-left: 156px;;font-size: 30px;color: white;\">用户名: </td>\n";
		msg += "<td><input type=\"text\" id=\"username\" name=\"username\" style=\"width: 280px;;height: 30px;\"></td>\n";
		msg += "</tr>\n";
		msg += "<tr>\n";
		msg += "<td style=\"padding-left: 156px;;font-size: 30px;color: white;\">密码: </td>\n";
		msg += "<td><input type=\"password\" id=\"password\" name=\"password\" style=\"width: 280px;;height: 30px;\"></td>\n";
		msg += "</tr>\n";
		msg += "<tr>\n";
		msg += "<td colspan=\"2\" align=\"center\"><input type=\"submit\" value=\"登录\" style=\"width: 100px;;height: 30px;\"></td>\n";
		msg += "</tr>\n";
        msg += "</tbody>\n";
		msg += "</table>\n";
		msg += "</td></tr>\n";
		if (!(loginMessage == ""))
			msg += "<tr><th>" + loginMessage + "</th></tr>\n";
		msg += "</table>\n";
		msg += "</form>\n";
		msg += "</body></html>\n";
		return msg;
	}
    
	private static String getJavaScriptFunctions()
	{
		String myScript = "function login()\n";
		myScript += "{\n";
		myScript += "	document.getElementById(\"username\").focus()\n";
		myScript += "}\n";
        
		return myScript;
	}
}