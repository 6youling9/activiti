package common.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * @author: 吴启迪
 * @date:2016-12-20 上午10:20:18
 * @version :
 * 
 */
public class WeatherUtil {
	/**
	* @Title: getTestWeather
	* @Description: TODO(由于免费的网站有获取次数限制，所以为了测试数据建立此方法)
	* @param @param city
	* @param @return    设定文件
	* @return String    返回类型
	* @throws
	*/
	public static String getTestWeather(String city){
		return "直辖市#北京#54511#54511.jpg#2017-2-21 9:36:07#-3℃/1℃#2月21日 阴#南风微风#2.gif#2.gif#今日天气实况：气温：0℃；风向/风力：西南风 1级；湿度：33%；紫外线强度：最弱。空气质量：良。#紫外线指数：最弱，辐射弱，涂擦SPF8-12防晒护肤品。"
				+"感冒指数：易发，大幅度降温，适当增加衣服。"
				+"穿衣指数：冷，建议着棉衣加羊毛衫等冬季服装。"
				+"洗车指数：不宜，有雪，雪水和泥水会弄脏爱车。"
				+"运动指数：较不宜，有降雪，请您在室内进行低强度运动。"
				+"空气污染指数：良，气象条件有利于空气污染物扩散。"
				+"#-3℃/9℃#2月22日 阴转晴#北风3-4级转微风#2.gif#0.gif#-2℃/8℃#2月23日 晴#南风转北风微风#0.gif#0.gif#北京位于华北平原西北边缘，市中心位于北纬39度，东经116度，四周被河北省围着，东南和天津市相接。全市面积一万六千多平方公里，辖12区6县，人口1100余万。北京为暖温带半湿润大陆性季风气候，夏季炎热多雨，冬季寒冷干燥，春、秋短促，年平均气温10-12摄氏度。北京是世界历史文化名城和古都之一。早在七十万年前，北京周口店地区就出现了原始人群部落“北京人”。而北京建城也已有两千多年的历史，最初见于记载的名字为“蓟”。公元前1045年北京成为蓟、燕等诸侯国的都城；公元前221年秦始皇统一中国以来，北京一直是中国北方重镇和地方中心；自公元938年以来，北京又先后成为辽陪都、金上都、元大都、明清国都。1949年10月1日正式定为中华人民共和国首都。北京具有丰富的旅游资源，对外开放的旅游景点达200多处，有世界上最大的皇宫紫禁城、祭天神庙天坛、皇家花园北海、皇家园林颐和园，还有八达岭、慕田峪、司马台长城以及世界上最大的四合院恭王府等各胜古迹。全市共有文物古迹7309项，其中国家文物保护单位42个，市级文物保护单位222个。北京的市树为国槐和侧柏，市花为月季和菊花。另外，北京出产的象牙雕刻、玉器雕刻、景泰蓝、地毯等传统手工艺品驰誉世界。#";
	}
	/**
	 * @param city
	 * @return
	 */
	public static String getWeather(String city){
		try {
			Document doc;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = getSoapInputStream(city);
			doc = db.parse(is);
			if(doc==null){
				return null;
			}
			NodeList nl = doc.getElementsByTagName("string");
			if(nl==null){
				return null;
			}
			StringBuffer sb = new StringBuffer();
			String nodeValue="";
			for (int count = 0; count < nl.getLength(); count++) {
				Node n = nl.item(count);
				if (n!=null&&n.getFirstChild()!=null){
					nodeValue=n.getFirstChild().getNodeValue();
					if ("查询结果为空！".equals(nodeValue)) {
						sb = new StringBuffer("#");
						break;
					}
					// 解析并以"#"为分隔符,拼接返回结果
					sb.append(nodeValue + "#");
				}
			}
			is.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static InputStream getSoapInputStream(String city){
		try {
			// 获取请求规范
			String soap = getSoapRequest(city);
			if (soap == null) {
				return null;
			}
			// 调用的天气预报webserviceURL
			URL url = new URL(
					"http://www.webxml.com.cn/WebServices/WeatherWebService.asmx");
			URLConnection conn = url.openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Length",
					Integer.toString(soap.length()));
			conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			// 调用的接口方法是“getWeatherbyCityName”
			conn.setRequestProperty("SOAPAction",
					"http://WebXml.com.cn/getWeatherbyCityName");
			OutputStream os = conn.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "utf-8");
			osw.write(soap);
			osw.flush();
			osw.close();
			// 获取webserivce返回的流
			InputStream is = conn.getInputStream();
			return is;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getSoapRequest(String city) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
				+ "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+ "<soap:Body><getWeatherbyCityName xmlns=\"http://WebXml.com.cn/\">"
				+ "<theCityName>" + city
				+ "</theCityName></getWeatherbyCityName>"
				+ "</soap:Body></soap:Envelope>");
		return sb.toString();
	}
		
}
