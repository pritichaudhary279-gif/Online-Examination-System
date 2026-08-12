<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Legacy multi-page exam replaced by one-question flow (StudentExamServlet + exam-single.jsp). --%>
<%
    response.sendRedirect(request.getContextPath() + "/dashboard");
%>
