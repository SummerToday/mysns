package mysns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MultipartConfig
public class snsController extends HttpServlet {

	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		Users user = null;
		if (session != null) {
			user = (Users) session.getAttribute("user");
		}

		if (user == null) {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("login_id")) {
						String loginId = cookie.getValue();
						UsersDAO userDao = new UsersDAO();
						try {
							userDao.open();
							user = userDao.findUserById(loginId);
							if (user != null) {
								session = request.getSession(true); // 세션을 새로 생성
								session.setAttribute("user", user);
								session.setAttribute("username", user.getName()); // 사용자 이름 저장
								session.setAttribute("loginTime", new Date()); // 로그인 시간 저장
							}
						} catch (SQLException e) {
							throw new ServletException("Database error while finding user by ID", e);
						} finally {
							try {
								userDao.close();
							} catch (SQLException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}
		}

		if (user == null && request.getParameter("action") == null) {
			response.sendRedirect("login.jsp");
			return;
		}

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
					throw new ServletException("Failed to retrieve image", e);
				}
			} else {
				try {
					List<Feeds> list = feedDao.getAll(user.getId());
					request.setAttribute("feedlist", list);
					getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
				} catch (Exception e) {
					throw new ServletException("Failed to retrieve feed list", e);
				}
			}
		} else {
			try {
				switch (action) {
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
				case "listFeeds":
					listFeeds(request, response, user);
					break;
				case "myFeeds":
					myFeeds(request, response, feedDao, user);
					break;
				case "showAllFeeds":
					showAllFeeds(request, response, feedDao, user);
					break;
				case "likeFeed":
					likeFeed(request, response, feedDao);
					break;
				default:
					// 정의되지 않은 action 값에 대한 처리
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (Exception e) {
				throw new ServletException("Database error", e);
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

	protected void login(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String id = request.getParameter("id");
		String password = request.getParameter("password");
		String rememberMe = request.getParameter("rememberMe");

		UsersDAO userDao = new UsersDAO();
		Users user = null;
		try {
			userDao.open();
			user = userDao.login(id, password);
		} catch (SQLException e) {
			throw new ServletException("Database error during login", e);
		} finally {
			try {
				userDao.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		if (user != null) {
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			session.setAttribute("username", user.getName()); // 사용자 이름 저장
			session.setAttribute("loginTime", new Date()); // 로그인 시간 저장
			session.setAttribute("login_id", id); // 로그인 ID 저장

			// 방문 횟수 증가 - 사용자별 쿠키 사용
			int visitCount = 1; // 기본값을 1로 설정
			boolean cookieExists = false;
			Cookie[] cookies = request.getCookies();

			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals("visitCount_" + id)) {
						visitCount = Integer.parseInt(cookie.getValue()) + 1; // 방문 횟수 증가
						cookie.setValue(String.valueOf(visitCount));
						response.addCookie(cookie); // 쿠키 업데이트
						cookieExists = true;
						break;
					}
				}
			}

			if (!cookieExists) {
				// 쿠키가 없는 경우 새로운 쿠키 생성
				Cookie visitCountCookie = new Cookie("visitCount_" + id, String.valueOf(visitCount));
				visitCountCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 동안 유지
				response.addCookie(visitCountCookie);
			}

			if ("true".equals(rememberMe)) {
				Cookie loginCookie = new Cookie("login_id", id);
				loginCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 동안 유지
				response.addCookie(loginCookie);
			}

			FeedsDAO feedDao = new FeedsDAO();
			try {
				List<Feeds> list = feedDao.getAll(user.getId());
				request.setAttribute("feedlist", list);
				getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
			} catch (Exception e) {
				throw new ServletException("Failed to retrieve feed list after login", e);
			} finally {
				try {
					feedDao.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			response.sendRedirect("login.jsp");
		}
	}

	// 로그아웃 메소드
	protected void logout(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 세션 정보 삭제
		HttpSession session = request.getSession();
		session.invalidate();

		// 자동 로그인 쿠키 삭제
		Cookie loginCookie = new Cookie("login_id", null);
		loginCookie.setMaxAge(0);
		loginCookie.setPath(request.getContextPath()); // 설정된 경로에 대해서만 유효하도록 설정
		response.addCookie(loginCookie);

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
		boolean is_Private = "true".equals(request.getParameter("private"));
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
				feed.setIs_Private(is_Private);
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

			List<Feeds> list = feedDao.getAll(user.getId());
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
			Feeds feed = feedDao.getFeedById(aid);
			Users user = (Users) request.getSession().getAttribute("user");

			// 권한 확인
			if (user == null || !feed.getId().equals(user.getId())) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Permission denied");
				return;
			}

			feedDao.delFeeds(aid);
			// 게시글 삭제 후 목록 페이지로 이동
			List<Feeds> list = feedDao.getAll(user.getId());
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

	private void editFeed(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao)
			throws ServletException, IOException {
		int aid = Integer.parseInt(request.getParameter("aid"));
		try {
			Feeds feed = feedDao.getFeedById(aid);
			Users user = (Users) request.getSession().getAttribute("user");

			// 권한 확인
			if (user == null || !feed.getId().equals(user.getId())) {
				request.setAttribute("errorMessage", "작성자만 수정이 가능합니다");
				request.setAttribute("feed", feed);
				getServletContext().getRequestDispatcher("/feedView.jsp").forward(request, response);
				return;
			}

			request.setAttribute("feed", feed);
			request.getRequestDispatcher("/editFeed.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get feed for editing");
		}
	}

	private void listFeeds(HttpServletRequest request, HttpServletResponse response, Users user)
			throws ServletException, IOException {
		FeedsDAO feedDao = new FeedsDAO();
		try {
			List<Feeds> list = feedDao.getAll(user.getId());
			request.setAttribute("feedlist", list);
			getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
		} catch (Exception e) {
			throw new ServletException("Failed to retrieve feed list", e);
		} finally {
			try {
				feedDao.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void updateFeed(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao)
			throws ServletException, IOException {
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
			Users user = (Users) request.getSession().getAttribute("user");

			// 권한 확인
			if (user == null || !feed.getId().equals(user.getId())) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Permission denied");
				return;
			}

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

	// "내가 쓴 글만 보기" 기능을 위한 메서드 추가
	private void myFeeds(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao, Users user)
			throws ServletException, IOException {
		try {
			feedDao.open();
			List<Feeds> myFeedsList = feedDao.getFeedsByUserId(user.getId());
			request.getSession().setAttribute("showMyFeeds", true);
			request.setAttribute("feedlist", myFeedsList);
			getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve feeds");
		} finally {
			try {
				feedDao.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// "전체 글 보기" 기능을 위한 메서드 추가
	private void showAllFeeds(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao, Users user)
			throws ServletException, IOException {
		try {
			feedDao.open();
			List<Feeds> allFeedsList = feedDao.getAll(user.getId());
			request.getSession().removeAttribute("showMyFeeds");
			request.setAttribute("feedlist", allFeedsList);
			getServletContext().getRequestDispatcher("/feedlist.jsp").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve feeds");
		} finally {
			try {
				feedDao.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void likeFeed(HttpServletRequest request, HttpServletResponse response, FeedsDAO feedDao)
			throws ServletException, IOException {
		int aid = Integer.parseInt(request.getParameter("aid"));
		try {
			boolean success = feedDao.likeFeed(aid);
			response.setContentType("text/plain");
			response.getWriter().write(success ? "success" : "fail");
		} catch (SQLException e) {
			throw new ServletException("Database error while liking feed", e);
		}
	}

}
