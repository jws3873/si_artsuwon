package kr.or.artsuwon.adminPfmc.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import kr.or.artsuwon.adminPfmc.model.vo.Performance;
import kr.or.artsuwon.adminPfmc.model.vo.PerformanceSkdl;
import kr.or.artsuwon.common.JDBCTemplate;

public class PfmcDAO {

	public int deletePfmc(Connection conn, String pfmcNo) {
		PreparedStatement pstmt = null;
		int resultRow = 0;
		
		String sql = "DELETE FROM PFMC WHERE PFMC_NO = ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pfmcNo);
			resultRow = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCTemplate.close(pstmt);
		}
		return resultRow;
	}
	
	public ArrayList<Performance> selectAllPfmc(Connection conn, int currentPage, int recordCountPerPage) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		ArrayList<Performance> list = new ArrayList<Performance>();
		
		int start = currentPage * recordCountPerPage - (recordCountPerPage - 1);
		int end = currentPage * recordCountPerPage;
		
		String sql = "SELECT *"
				+ "    FROM ("
				+ "          SELECT ROW_NUMBER() OVER(ORDER BY A.PFMC_NO) AS NUM"
				+ "               , A.PFMC_NO"
				+ "               , MIN(A.CATEGORY) CATEGORY"
				+ "               , MIN(A.TITLE) TITLE"
				+ "               , TO_CHAR(MIN(B.PFMC_DATE), 'YYYY-MM-DD') PFMC_DATE"
				+ "               , TO_CHAR(MIN(B.PFMC_DATE), 'HH24:MI') PFMC_TIME"
				+ "               , MIN(B.LOCATION) LOCATION"
				+ "               , COUNT(*) OVER() AS CNT"
				+ "            FROM PFMC A"
				+ "            LEFT JOIN PFMC_SCHEDULE B ON (A.PFMC_NO = B.PFMC_NO)"
				+ "           GROUP BY A.PFMC_NO"
				+ "          )"
				+ "WHERE NUM BETWEEN ? AND ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, start);
			pstmt.setInt(2, end);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				Performance pfmc = new Performance();
				pfmc.setNum(rset.getInt("NUM"));
				pfmc.setPfmcNo(rset.getInt("PFMC_NO"));
				pfmc.setCategory(rset.getString("CATEGORY"));
				pfmc.setTitle(rset.getString("TITLE"));
				pfmc.setPfmcDate(rset.getString("PFMC_DATE"));
				pfmc.setPfmcTime(rset.getString("PFMC_TIME"));
				pfmc.setLocation(rset.getString("LOCATION"));
				pfmc.setTotalCount(rset.getInt("CNT"));
				list.add(pfmc);		
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rset);
			JDBCTemplate.close(pstmt);
		}
		return list;
	}
	
	public String getPageNavi(Connection conn, int naviCountPerPage, int recordCountPerPage, int currentPage, String pageNaviUrl, ArrayList<Performance> pfmc, String qs) {
		String queryString = qs;

		if (pfmc.isEmpty()) {
			return "";
		} 
		
		int totalRecordCount = pfmc.get(0).getTotalCount();
		//int TotalRecordCount = totalCount(conn); //전체 개수 (58)
		int TotalPageCount = 0; //전체 개수를 recordPerPage로 나누면 5.8페이지(6페이지)
		
		//전체 개수 (전체 개수를 recordPerPage로 나누면 5.8 -> 6으로 올리기)
		if ((totalRecordCount % recordCountPerPage) > 0) { 
			TotalPageCount = (totalRecordCount / recordCountPerPage) + 1;
		} else {
			TotalPageCount = totalRecordCount / recordCountPerPage;
		}
		
		//네비바 시작과 끝 (예 : <<4,5,6>> 중 4 와 6)
		int startNavi = (((currentPage - 1) / naviCountPerPage) * naviCountPerPage) + 1;
		int endNavi = startNavi + (naviCountPerPage - 1);
		
		//네비바 완전 마지막장
		if(endNavi > TotalPageCount) {
			endNavi = TotalPageCount;
		}
		
		//페이지 네비바
		StringBuilder sb = new StringBuilder();
		if(startNavi != 1) {
			sb.append(
				"<li class=\"page-item\">"
				+ "<a class=\"page-link\" href='" + pageNaviUrl + "?currentPage=" + (startNavi-1) + "&" + queryString + "' aria-label=\"Previous\">"
				+ "<span aria-hidden=\"true\">«</span>"
				+ "</a>"
				+ "</li>"
				);
			}	
		
		for (int i = startNavi; i <= endNavi; i++) {
			if(i == currentPage) {
				sb.append(
						"<li class=\"page-item active\">"
						+ "<a class=\"page-link\" href='"+ pageNaviUrl + "?currentPage=" + i + "&" + queryString + "'>" + i + "</a>"
						+ "</li>"
						);
			} else {
				sb.append(
						"<li class=\"page-item\">"
						+ "<a class=\"page-link\" href='" + pageNaviUrl + "?currentPage=" + i + "&" + queryString + "'>" + i + "</a>"
						+ "</li>"
						);
			}
		}
		
		if (endNavi != TotalPageCount) {
			sb.append(
				"<li class=\"page-item\">"
				+ " <a class=\"page-link\" href='"+ pageNaviUrl +"?currentPage=" + (endNavi+1) + "&" + queryString + "' aria-label=\"Next\">"
				+ " <span aria-hidden=\"true\">»</span>"
				+ " </a>"
				+ " </li>"
			);
		}
	return sb.toString();
}

	
	public int insertPfmc(Connection conn, Performance pfmc) {
		PreparedStatement pstmt = null;
		PreparedStatement pstmt2 = null;
		int resultRow = 0;
		ResultSet rset = null;
		int pfmcPk = 0;
		
		String pfmcSql = "INSERT INTO PFMC VALUES(PFMC_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,'0')";
		String pfmcSkdlSql = "INSERT INTO PFMC_SCHEDULE VALUES(PFMC_SCHEDULE_SEQ.NEXTVAL,?,?,?,TO_DATE(?, 'YYYY-MM-DD HH24:MI'),?)";
		
			try {
				pstmt = conn.prepareStatement(pfmcSql, new String[]{"pfmc_no"});
				pstmt2 = conn.prepareStatement(pfmcSkdlSql);
				
				pstmt.setString(1, pfmc.getCategory());
				pstmt.setString(2, pfmc.getTitle());
				pstmt.setString(3, pfmc.getConductor());
				pstmt.setString(4, pfmc.getCollaborator());
				pstmt.setString(5, pfmc.getPrograms());
				pstmt.setString(6, pfmc.getIntroduction());
				pstmt.setString(7, pfmc.getProgramNote());
				pstmt.setString(8, pfmc.getRelatedPackage());
				pstmt.setString(9, pfmc.getPhoto());
				pstmt.setString(10, pfmc.getMemo());
				resultRow = pstmt.executeUpdate();
				
				if (resultRow > 0) {
					rset = pstmt.getGeneratedKeys();
					if(rset.next()) {
						pfmcPk = rset.getInt(1); 
						
						pstmt2.setInt(1, pfmcPk);
						pstmt2.setString(2, pfmc.getLocation());
						pstmt2.setString(3, pfmc.getPrice());
						pstmt2.setString(4, pfmc.getPfmcDate() +" "+ pfmc.getPfmcTime());
						
						//좌석제한 String -> int 변경
						String restrictionName = pfmc.getRestriction();
						int restriction = 0; 
						switch (restrictionName) {
						case "1단계": {
							restriction = 1; break;
							}
						case "2단계": {
							restriction = 2; break;
							}
						case "3단계": {
							restriction = 3; break;
							}
						}
						pstmt2.setInt(5, restriction);
						resultRow += pstmt2.executeUpdate();
					}	
				}
		        } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				} finally {
					JDBCTemplate.close(pstmt);
					JDBCTemplate.close(pstmt2);
			}
			return resultRow;
	}

	
	public Performance showPfmcInfo(Connection conn, int pfmcNo) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		Performance pfmc = null;
		
		String sql = "SELECT "
				+ "CATEGORY, TITLE, CONDUCTOR, COLLABORATOR, PROGRAMS, INTRODUCTION, PROGRAM_NOTE, RELATED_PACKAGE, PHOTO, MEMO "
				+ "FROM PFMC WHERE PFMC_NO=?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pfmcNo);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				pfmc = new Performance();
				pfmc.setCategory(rset.getString("CATEGORY"));
				pfmc.setTitle(rset.getString("TITLE"));
				pfmc.setConductor(rset.getString("CONDUCTOR"));
				pfmc.setCollaborator(rset.getString("COLLABORATOR"));
				pfmc.setPrograms(rset.getString("PROGRAMS"));
				pfmc.setIntroduction(rset.getString("INTRODUCTION"));
				pfmc.setProgramNote(rset.getString("PROGRAM_NOTE"));
				pfmc.setRelatedPackage(rset.getString("RELATED_PACKAGE"));
				pfmc.setPhoto(rset.getString("PHOTO"));
				pfmc.setMemo(rset.getString("MEMO"));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(rset);
			JDBCTemplate.close(pstmt);
		}
		return pfmc;
	}

	public ArrayList<PerformanceSkdl> selectPfmcSkdl(Connection conn, int pfmcNo) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		ArrayList<PerformanceSkdl> pfmcSkdlList = new ArrayList<PerformanceSkdl>();
		
		String sql = "select schedule_no"
				+ ", pfmc_no"
				+ ", location"
				+ ", price"
				+ ", to_char(pfmc_date,'YYYY-MM-DD HH24:MI') pfmc_date"
				+ ", (select code_name from common_code where category_code = '1004' and code = a.restriction) restriction_name"
				+ " from pfmc_schedule a"
				+ " WHERE PFMC_NO=?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, pfmcNo);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				PerformanceSkdl pfmcSkdl = new PerformanceSkdl();
				pfmcSkdl.setScheduleNo(rset.getInt("SCHEDULE_NO"));
				pfmcSkdl.setPfmcNo(rset.getInt("PFMC_NO"));
				pfmcSkdl.setLocation(rset.getString("LOCATION"));
				pfmcSkdl.setPrice(rset.getString("PRICE"));
				
				//pfmc_date가 db에선 date형이지만 to_char로 문자로 변환해서 날짜, 시간 분리
				String dateTime = rset.getString("PFMC_DATE"); 
				pfmcSkdl.setPfmcDate(dateTime.substring(0, 10));
				pfmcSkdl.setPfmcTime(dateTime.substring(11, 16));
				
				pfmcSkdl.setRestrictionName(rset.getString("RESTRICTION_NAME"));
				pfmcSkdlList.add(pfmcSkdl);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCTemplate.close(rset);
			JDBCTemplate.close(pstmt);
		}
		return pfmcSkdlList;
		
	}

	public int updatePfmc(Connection conn, Performance pfmc) {
		PreparedStatement pstmt = null;
		int resultRow = 0;
		String sql = "UPDATE PFMC SET CATEGORY=?, TITLE=?, CONDUCTOR=?, COLLABORATOR=?, PROGRAMS=?,"
				+ " INTRODUCTION=?, PROGRAM_NOTE=?, RELATED_PACKAGE=?, MEMO=?, PHOTO=?"
				+ " WHERE PFMC_NO=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pfmc.getCategory());
			pstmt.setString(2, pfmc.getTitle());
			pstmt.setString(3, pfmc.getConductor());
			pstmt.setString(4, pfmc.getCollaborator());
			pstmt.setString(5, pfmc.getPrograms());
			pstmt.setString(6, pfmc.getIntroduction());
			pstmt.setString(7, pfmc.getProgramNote());
			pstmt.setString(8, pfmc.getRelatedPackage());
			pstmt.setString(9, pfmc.getMemo());
			pstmt.setString(10, pfmc.getPhoto());
			pstmt.setInt(11, pfmc.getPfmcNo());
			resultRow = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCTemplate.close(pstmt);
		}
		return resultRow;
	}

	public int insertPfmcSkdl(Connection conn, PerformanceSkdl pfmcSkdl) {
		PreparedStatement pstmt = null;
		int resultRow = 0;
		ResultSet rset = null;
		int scheduleNo = 0;
		
		String sql = "INSERT INTO PFMC_SCHEDULE VALUES(PFMC_SCHEDULE_SEQ.NEXTVAL,?,?,?,TO_DATE(?, 'YYYY-MM-DD HH24:MI'),?)";
		
		try {
			pstmt = conn.prepareStatement(sql, new String[]{"schedule_no"});
			pstmt.setInt(1, pfmcSkdl.getPfmcNo());
			pstmt.setString(2, pfmcSkdl.getLocation());
			pstmt.setString(3, pfmcSkdl.getPrice());
			pstmt.setString(4, pfmcSkdl.getPfmcDate() +" "+ pfmcSkdl.getPfmcTime());
			pstmt.setInt(5, pfmcSkdl.getRestriction());
			resultRow = pstmt.executeUpdate();
			
			if (resultRow > 0) {
				rset = pstmt.getGeneratedKeys();
				if(rset.next()) {
					scheduleNo = rset.getInt(1);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCTemplate.close(pstmt);
		}
		return scheduleNo;
	}

	public int deletePfmcSkdl(Connection conn, int scheduleNo) {
		PreparedStatement pstmt = null;
		int resultRow = 0;
		
		String sql = "DELETE FROM PFMC_SCHEDULE WHERE SCHEDULE_NO = ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, scheduleNo);
			resultRow = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			JDBCTemplate.close(pstmt);
		}
		return resultRow;
}
	
	
	public ArrayList<Performance> searchPfmc(Connection conn, String srchDate, String srchCategory, String srchPfmcName, int currentPage, int recordCountPerPage) {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		ArrayList<Performance> srchList = new ArrayList<Performance>();
		
		int start = currentPage * recordCountPerPage - (recordCountPerPage - 1);
		int end = currentPage * recordCountPerPage;
		
		String sql = "select * from"
				+ "                    ("
				+ "                    select ROW_NUMBER() OVER(ORDER BY A.PFMC_NO) AS NUM"
				+ "                    , A.PFMC_NO"
				+ "                    , min(A.TITLE) TITLE"
				+ "                    , min(A.CATEGORY) CATEGORY"
				+ "                    , to_char(min(B.pfmc_date),'YYYY-MM-DD') pfmc_date"
				+ "                    , min(B.location) LOCATION"
				+ "                    , COUNT(*) OVER() AS CNT"
				+ "                    from pfmc A"
				+ "                    left join pfmc_schedule B on (A.PFMC_NO = B.PFMC_NO)"
				+ "                    WHERE NVL(to_char(B.pfmc_date,'YYYY-MM-DD'), '9999-12-31') = nvl(?, NVL(to_char(B.pfmc_date,'YYYY-MM-DD'), '9999-12-31'))"
				+ "                    and A.category = nvl(?, A.category)"
				+ "                    and A.title like '%' || ? || '%'"
				+ "                    group by a.pfmc_no"
				+ "                    ) "
				+ "where num between ? and ?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, srchDate);
			pstmt.setString(2, srchCategory);
			pstmt.setString(3, srchPfmcName);
			pstmt.setInt(4, start);
			pstmt.setInt(5, end);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				Performance pfmc = new Performance();
				pfmc.setNum(rset.getInt("NUM"));
				pfmc.setPfmcNo(rset.getInt("PFMC_NO"));
				pfmc.setTitle(rset.getString("TITLE"));
				pfmc.setCategory(rset.getString("CATEGORY"));
				pfmc.setPfmcDate(rset.getString("PFMC_DATE"));
				pfmc.setLocation(rset.getString("LOCATION"));
				pfmc.setTotalCount(rset.getInt("CNT"));
				srchList.add(pfmc);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			JDBCTemplate.close(rset);
			JDBCTemplate.close(pstmt);
		}
		return srchList;
	}
}
