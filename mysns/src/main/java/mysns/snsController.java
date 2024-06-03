package mysns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MultipartConfig
public class snsController extends HttpServlet {
    
	protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        String action = request.getParameter("action");
        FeedsDAO feedDao = new FeedsDAO();
        if (action == null) {
        	String aidParam = request.getParameter("aid");
            if (aidParam != null && !aidParam.isEmpty()) {
                try {
                    // aid 값이 존재하면 이미지를 출력
                    byte[] imageBytes = feedDao.getImageById(Integer.parseInt(aidParam));
                    response.setContentType("image/jpeg");
                    OutputStream outputStream = response.getOutputStream();
                    outputStream.write(imageBytes);
                    outputStream.close();
                } catch (NumberFormatException | SQLException e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve image");
                }
            } else {
                List<Feeds> list = feedDao.getAll();
                request.setAttribute("feedlist", list);
                getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
            }
        } else {
            switch(action) {
                case "signup":
                    signup(request, response);
                    break;
                case "login":
                    login(request, response);
                    break;
                case "logout":
                    logout(request, response);
                    break;
                case "write":
                    write(request, response);
                    break;
                case "delFeeds":
                    delFeeds(request, response);
                    break;
                case "viewFeed":
                    viewFeed(request, response);
                    break;
                case "editFeed":
                    editFeed(request, response, feedDao);
                    break;
                case "updateFeed":
                    updateFeed(request, response, feedDao);
                    break;
                default:
                    // 정의되지 않은 action 값에 대한 처리
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    
    // 사용자 등록 메소드
    protected void signup(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // HTML 폼으로부터 입력된 데이터를 가져와서 UsersDAO의 signup 메소드를 통해 등록
        String id = request.getParameter("id");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        
        UsersDAO userDao = new UsersDAO();
        try {
            userDao.open();
        
            Users user = new Users();
            user.setId(id);
            user.setPassword(password);
            user.setName(name);
            
            userDao.signup(user);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                userDao.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        // 등록 후 로그인 페이지로 이동
        response.sendRedirect("login.jsp");
    }
    
    // 로그인 메소드
    protected void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // HTML 폼으로부터 입력된 사용자 정보를 가져와서 UsersDAO의 login 메소드를 통해 확인
        String id = request.getParameter("id");
        String password = request.getParameter("password");
        
        UsersDAO userDao = new UsersDAO();
        Users user = null;
        try {
            userDao.open();
            user = userDao.login(id, password);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                userDao.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        
        if(user != null) {
            // 로그인 성공한 경우, 세션에 사용자 정보 저장
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // 게시글 목록 페이지로 이동
            FeedsDAO feedDao = new FeedsDAO();// *로그인 시 이전 게시물들 불러오기* - 로그인 후 작성한 게시글이 추가된 후에 게시글 목록을 다시 불러오고, feedlist.jsp로 이동
            List<Feeds> list = feedDao.getAll();
            request.setAttribute("feedlist", list);
            getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
        } else {
            // 로그인 실패한 경우, 로그인 페이지로 이동
            response.sendRedirect("login.jsp");
        }
    }
    
    // 로그아웃 메소드
    protected void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 세션 정보 삭제
        HttpSession session = request.getSession();
        session.invalidate();
        
        // 로그아웃 메시지를 출력하고 로그인 페이지로 이동
        response.sendRedirect("logout.jsp");
    }
    
    protected void write(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String content = request.getParameter("content");
        Timestamp created_at = new Timestamp(System.currentTimeMillis());
        byte[] imageData = null;

        try {
            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                try (InputStream inputStream = imagePart.getInputStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    imageData = outputStream.toByteArray();
                }
            }

            FeedsDAO feedDao = new FeedsDAO();
            try {
                feedDao.open();
                Feeds feed = new Feeds();
                feed.setId(user.getId());
                feed.setContent(content);
                feed.setCreated_at(created_at);
                if (imageData != null) {
                    Blob imageBlob = new javax.sql.rowset.serial.SerialBlob(imageData);
                    feed.setImage(imageBlob);
                }
                feedDao.write(feed);
            } finally {
                try {
                    feedDao.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            List<Feeds> list = feedDao.getAll();
            request.setAttribute("feedlist", list);
            getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);

        } catch (IOException | ServletException e) {
            e.printStackTrace();
            throw new ServletException("Failed to upload image and write feed", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Failed to write feed", e);
        }
    }
    
    
    // 게시글 삭제 로직 추가
    private void delFeeds(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
         int aid = Integer.parseInt(request.getParameter("aid"));
         FeedsDAO feedDao = new FeedsDAO();
         try {
            feedDao.open();
            feedDao.delFeeds(aid);
            // 게시글 삭제 후 목록 페이지로 이동
            List<Feeds> list = feedDao.getAll();
               request.setAttribute("feedlist", list);
               getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
         } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete news");
         } finally {
            try {
               feedDao.close();
            } catch (SQLException e) {
               e.printStackTrace();
            }
         }
      }
    
    private void viewFeed(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int aid = Integer.parseInt(request.getParameter("aid"));
            FeedsDAO feedDao = new FeedsDAO();
            try {
                feedDao.open();
                Feeds feed = feedDao.getFeedById(aid);
                request.setAttribute("feed", feed);
                getServletContext().getRequestDispatcher("/feedView.jsp").forward(request, response);
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve feed");
            } finally {
                try {
                    feedDao.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing aid parameter");
        }
    }
    
    private void editFeed(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao) throws ServletException, IOException {
        int aid = Integer.parseInt(request.getParameter("aid"));
        try {
            Feeds feed = feedDao.getFeedById(aid);
            request.setAttribute("feed", feed);
            request.getRequestDispatcher("/editFeed.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get feed for editing");
        }
    }

    private void updateFeed(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao) throws ServletException, IOException {
        int aid = Integer.parseInt(request.getParameter("aid"));
        String content = request.getParameter("content");
        Blob imageBlob = null;

        // 이미지 파일 업로드 처리
        Part filePart = request.getPart("uploadFile");
        if (filePart != null && filePart.getSize() > 0) {
            try {
                InputStream fileContent = filePart.getInputStream();
                byte[] imageData = fileContent.readAllBytes();
                imageBlob = new SerialBlob(imageData);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ServletException("Failed to process image file", e);
            }
        }

        // 게시글 업데이트
        try {
            Feeds feed = feedDao.getFeedById(aid);
            feed.setContent(content);
            if (imageBlob != null) {
                feed.setImage(imageBlob);
            }
            feedDao.updateFeed(feed);
            response.sendRedirect("snsController?action=viewFeed&aid=" + aid);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update feed");
        }
    }


}
