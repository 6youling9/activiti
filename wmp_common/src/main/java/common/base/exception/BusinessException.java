package common.base.exception;

public class BusinessException extends Exception {
	private static final long serialVersionUID = 1L;
	private String code;
	private Exception exception;
	public BusinessException(DAOException exception) {
		this.exception = exception;
		this.code = exception.getCode();
	}
	public BusinessException(String code) {
		this.code = code;
		this.exception = new Exception();
	}
	public BusinessException(Exception exception,String code) {
		if(isDAOException(exception)){
			DAOException daoException = new DAOException(exception);
			this.exception = daoException;
			this.code = daoException.getCode();
		} else {
			this.code = code;
			this.exception = new Exception();
		}
	}
	private boolean isDAOException(Exception exception) {
		String className = exception.getClass().getName();
		if(exception instanceof DAOException || 
				className.contains("com.mysql.jdbc.exceptions") || 
				className.contains("org.mybatis.spring.MyBatisSystemException") ||
				className.contains("org.springframework.jdbc") ){
			return true;
		}
		return false;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Override
	public String toString() {
		return getCode();
	}
}
