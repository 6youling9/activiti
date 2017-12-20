
package common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TableToEntity {
	
	public static void main(String[] args) throws Exception {
		String schema = "ioc_epm";
		String tableName = "epm_command_expert"; 
		createFields(schema,tableName);
	}

	public static void createFields(String schema,String tableName) throws Exception {
		// 数据库链接
		String url = "jdbc:mysql://172.16.17.37:3306/dgioc_cms?useUnicode=true&characterEncoding=utf8";
		String username = "root";
		String passwd = "bonc";
		String classDriver = "com.mysql.jdbc.Driver";
		Class.forName(classDriver);
		Connection connection = DriverManager.getConnection(url, username,
				passwd);
		Statement statement = connection.createStatement();
		String sql = "select column_name,column_comment,column_key,is_nullable,data_type from information_schema.columns where table_schema =  '"+schema+"' and table_name='"+tableName+"'";
		ResultSet resultSet = statement.executeQuery(sql);

		// 返回结果resultSet转list
		List<HashMap<String, Object>> list = resultSetToList(resultSet);

		System.out.println("===================================================");
		for (HashMap<String, Object> column : list) {
			StringBuffer sb=new StringBuffer();
			sb.append("/** ").append(column.get("COLUMN_COMMENT").toString());
			String TYPE=column.get("DATA_TYPE").toString();
			if(TYPE.contains("date")){
				sb.append(" */\nprivate Date ");
			}else if(TYPE.contains("int")) {
				sb.append(" */\nprivate Integer ");
			}else{
				sb.append(" */\nprivate String ");
			}
                sb.append(getSName(column.get("COLUMN_NAME").toString()) + ";");				
		    System.out.println(sb.toString());
		}
		
		
		//
		
		System.out.println("\nselect==============================================\n");
		for (HashMap<String, Object> column : list) {
//			System.out.println("/** " + column.get("COLUMN_COMMENT").toString()
//					+ " */\nprivate String "
//					+ getSName(column.get("COLUMN_NAME").toString()) + ";");
			System.out.println(column.get("COLUMN_NAME")+" AS " + getSName(column.get("COLUMN_NAME").toString())+",");
		}
		System.out.println("\nsave==============================================\n");
		for (HashMap<String, Object> column : list) {
			String field = getSName(column.get("COLUMN_NAME").toString());
			System.out.println("<if test=\""+field+" != null and "+field+" !='' \">\n\t"+column.get("COLUMN_NAME").toString().toLowerCase()+",\n</if>");
		}
		System.out.println("\nsave-values==============================================\n");
		for (HashMap<String, Object> column : list) {
			String field = getSName(column.get("COLUMN_NAME").toString());
			System.out.println("<if test=\""+field+" != null and "+field+" !='' \">\n\t#{"+field+"},\n</if>");
		}
		System.out.println("\nupdate==============================================\n");
		for (HashMap<String, Object> column : list) {
			String field = getSName(column.get("COLUMN_NAME").toString());
			System.out.println("<if test=\""+field+" != null and "+field+" !='' \">\n\t"+column.get("COLUMN_NAME").toString().toLowerCase()+" = #{"+field+"},\n</if>");
		}
	}

	private static String getSName(String columnName) {
		if (("" + columnName.charAt(columnName.length() - 1)).equals("_")) {
			return columnName;
		} else {
			String name = "";
			String[] str = columnName.split("_");
			for (int i = 0; i < str.length; i++) {
				if (i == 0) {
					name += str[i].toLowerCase();
				} else {
					name += (("" + str[i].charAt(0)).toUpperCase() + str[i].substring(1)
							.toLowerCase());
				}
			}
			return name;
		}
	}

	private static List<HashMap<String, Object>> resultSetToList(ResultSet rs)
			throws SQLException {
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		ResultSetMetaData md = rs.getMetaData();
		int columnCount = md.getColumnCount();
		while (rs.next()) {
			HashMap<String, Object> rowData = new HashMap<String, Object>();
			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(rowData);
		}
		return list;
	}

}
