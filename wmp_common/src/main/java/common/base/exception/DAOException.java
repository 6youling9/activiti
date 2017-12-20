package common.base.exception;

import common.util.ExceptionCodeUtil;

/**
 * 数据操作异常类
 * @author 左明强
 */
public class DAOException extends Exception {
	private static final long serialVersionUID = 1L;
	private String code;
	private Exception exception;
	public DAOException(Exception exception) {
		this.exception = exception;
		this.code = getCode();
	}
	public DAOException(String code) {
		this.code = code;
		this.exception = new Exception();
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	@Override
	public String toString() {
		return getCode();
	}
	public String getCode() {
		if(code!=null){
			return code;
		}else{
			//spring
//	CleanupFailureDataAccessException	一项操作成功地执行，但在释放数据库资源时发生异常（例如，关闭一个Connection）
//	DataAccessResourceFailureException	数据访问资源彻底失败，例如不能连接数据库
//	DataIntegrityViolationException	Insert或Update数据时违反了完整性，例如违反了惟一性限制
//	DataRetrievalFailureException	某些数据不能被检测到，例如不能通过关键字找到一条记录
//	DeadlockLoserDataAccessException	当前的操作因为死锁而失败
//	IncorrectUpdateSemanticsDataAccessException	Update时发生某些没有预料到的情况，例如更改超过预期的记录数。当这个异常被抛出时，执行着的事务不会被回滚
//	InvalidDataAccessApiusageException	一个数据访问的JAVA API没有正确使用，例如必须在执行前编译好的查询编译失败了
//	invalidDataAccessResourceUsageException	错误使用数据访问资源，例如用错误的SQL语法访问关系型数据库
//	OptimisticLockingFailureException	乐观锁的失败。这将由ORM工具或用户的DAO实现抛出
//	TypemismatchDataAccessException	Java类型和数据类型不匹配，例如试图把String类型插入到数据库的数值型字段中
//	UncategorizedDataAccessException	有错误发生，但无法归类到某一更为具体的异常中
			//mysql
//	com.mysql.jdbc.exceptions.
			switch(exception.getClass().getSimpleName()){
			case "DataAccessResourceFailureException":
				return ExceptionCodeUtil.IOCE_AD001;//"无法连接数据库！请联系系统管理员！"
			case "CleanupFailureDataAccessException" :
				return ExceptionCodeUtil.IOCE_AD002;//"释放数据库资源异常!";
			default:
				return ExceptionCodeUtil.IOCE_AD000; //未知数据访问异常！请联系技术支持团队！
			}
		}
	}
	public void setCode(String code) {
		this.code = code;
	}
}
