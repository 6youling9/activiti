package common.util;

import java.util.List;

public class PageResult {
	/**
	 * 总行数
	 */
	private Integer total;
	/**
	 * 数据行
	 */
	private List<PageData> rows;
	
	public PageResult(){
	}

	public PageResult(Integer total, List<PageData> rows) {
		super();
		this.total = total;
		this.rows = rows;
	}

	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public List<PageData> getRows() {
		return rows;
	}
	public void setRows(List<PageData> rows) {
		this.rows = rows;
	}
}