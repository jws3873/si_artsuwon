package kr.or.artsuwon.reservation.model.dao;

import kr.or.artsuwon.common.JDBCTemplate;
import kr.or.artsuwon.reservation.model.vo.Reservation;
import kr.or.artsuwon.reservation.model.vo.SeatInfomation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReservationDAO {

    /**
     * 공연일정으로 모든 예약정보를 조회하는 메서드
     * @param conn 연결정보
     * @param scheduleNo  공연일정번호
     * @return 예약정보 리스트
     * @author 신현진
     */
    public ArrayList<Reservation> selectAllReservationByPerfSchedule(Connection conn, int scheduleNo) {
        final String QUERY = "SELECT * FROM reservation WHERE schedule_no = ? ";
        ResultSet rset = null;
        ArrayList<Reservation> list = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(QUERY)) {
            pstmt.setInt(1, scheduleNo);
            rset = pstmt.executeQuery();

            while (rset.next()) {
                Reservation reservation = new Reservation(
                    rset.getString("RESERAVATION_NO"),
                    rset.getInt("PFMC_NO"),
                    rset.getString("INVOICE_NO"),
                    rset.getString("PAY_METHOD"),
                    rset.getString("RESERVATION_ID"),
                    rset.getDate("RESERVATION_DATE"),
                    rset.getInt("RESERVATION_PRICE"),
                    rset.getString("SEAT_CODE"));
                list.add(reservation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rset);
            // AutoClosable pstmt
        }

        return list;
    }


    /**
     * 좌석정보를 구하는 메서드
     * @param conn 연결정보
     * @param restriction 제약조건
     * @return 좌석정보 리스트
     * @author 신현진
     */
    public ArrayList<SeatInfomation> selectAllSeatsByRestriction(Connection conn, int restriction) {
        final String QUERY = "SELECT * FROM seat_info WHERE restriction < ?";
        ResultSet rset = null;
        ArrayList<SeatInfomation> list = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(QUERY)) {
            pstmt.setInt(1, restriction);
            rset = pstmt.executeQuery();

            while (rset.next()) {
                SeatInfomation seatInfomation = new SeatInfomation(
                    rset.getString("CODE"),
                    rset.getString("LOCATION").charAt(0),
                    rset.getString("GRADE").charAt(0),
                    rset.getString("SPECIAL_NEED").charAt(0),
                    rset.getString("RESTRICTION").charAt(0),
                    rset.getString("MEMO"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCTemplate.close(rset);
            // AutoClosable pstmt
        }

        return list;
    }
}
