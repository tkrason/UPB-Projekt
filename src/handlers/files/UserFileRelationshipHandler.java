package handlers.files;

import config.UrlPaths;
import database.classes.Comment;
import database.classes.FileData;
import database.exceptions.DatabaseNotLoadedException;
import domain.utils.UrlUtils;
import services.auth.CookieAuthorization;
import services.files.CommentServiceImpl;
import services.files.FileServiceImpl;
import services.files.interfaces.CommentService;
import services.files.interfaces.FileService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class UserFileRelationshipHandler extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private FileService fileService = new FileServiceImpl();


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = (String) request.getSession().getAttribute("username");
        String fileId = request.getParameter("fileId");
        String selectedUser = request.getParameter("selectedUser");
        String selectedType = request.getParameter("relationshipType");

        if (CookieAuthorization.isNotLoggedIn(request)) {
            response.sendRedirect(UrlUtils.getUrlFromRequest(request) + UrlPaths.LOGIN_PATH);
            return;
        }

        try {
            if (!canPrivilegeBeGranted(username, fileId)) {
                response.sendRedirect(UrlUtils.getUrlFromRequest(request) + UrlPaths.MY_FILES_PATH);
                return;
            }

            switch (selectedType) {
                case "OWNER":
                    fileService.addOwnerToFile(selectedUser, fileId);
                    break;
                case "GUEST":
                    fileService.addGuestToFile(selectedUser, fileId);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

        } catch (DatabaseNotLoadedException e) {
            e.printStackTrace();
        }

        response.sendRedirect(UrlUtils.getUrlFromRequest(request) + UrlPaths.FILE_DETAIL_PATH + "?fileId=" + fileId);
    }

    private boolean canPrivilegeBeGranted(String username, String fileId) throws DatabaseNotLoadedException {
        try {
            return fileService.isUserFileOwner(username, fileId) && fileService.fileExists(fileId);
        } catch (DatabaseNotLoadedException e) {
            e.printStackTrace();
            throw e;
        }
    }

}