package cn.edu.zju.controller;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.DrugProfessionalInfo;
import cn.edu.zju.bean.User;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugFavoriteDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.DrugProfessionalInfoDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private DrugDao drugDao = new DrugDao();
    private DrugFavoriteDao drugFavoriteDao = new DrugFavoriteDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
    private DrugProfessionalInfoDao drugProfessionalInfoDao = new DrugProfessionalInfoDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/drugs", this::drugs);
        dispatcher.registerGetMapping("/drugLabels", this::drugLabels);
        dispatcher.registerGetMapping("/dosingGuideline", this::dosingGuideline);
        dispatcher.registerGetMapping("/drugProfessionalInfo", this::drugProfessionalInfo);
        dispatcher.registerPostMapping("/drugFavorite", this::drugFavorite);
    }

    public void drugs(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Drug> drugs;
        if (keyword != null && !keyword.trim().isEmpty()) {
            drugs = drugDao.findByKeyword(keyword);
        } else {
            drugs = drugDao.findAll();
        }
        markDrugFavorites(request, drugs);
        request.setAttribute("drugs", drugs);
        request.getRequestDispatcher("/views/drugs.jsp").forward(request, response);
    }

    public void drugLabels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<DrugLabel> drugLabels;
        if (keyword != null && !keyword.trim().isEmpty()) {
            drugLabels = drugLabelDao.findByKeyword(keyword);
        } else {
            drugLabels = drugLabelDao.findAll();
        }
        markDrugLabelFavorites(request, drugLabels);
        request.setAttribute("drugLabels", drugLabels);
        request.getRequestDispatcher("/views/drug_labels.jsp").forward(request, response);
    }

    public void dosingGuideline(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<DosingGuideline> dosingGuidelines;
        if (keyword != null && !keyword.trim().isEmpty()) {
            dosingGuidelines = dosingGuidelineDao.findByKeyword(keyword);
        } else {
            dosingGuidelines = dosingGuidelineDao.findAll();
        }
        markDosingGuidelineFavorites(request, dosingGuidelines);
        request.setAttribute("dosingGuidelines", dosingGuidelines);
        request.getRequestDispatcher("/views/dosing_guideline.jsp").forward(request, response);
    }

    public void drugProfessionalInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = trimToNull(request.getParameter("keyword"));
        String sourceType = trimToNull(request.getParameter("sourceType"));
        String evidenceLevel = trimToNull(request.getParameter("evidenceLevel"));
        List<DrugProfessionalInfo> drugProfessionalInfos = drugProfessionalInfoDao.findByFilters(keyword, sourceType, evidenceLevel);
        markDrugProfessionalInfoFavorites(request, drugProfessionalInfos);
        request.setAttribute("drugProfessionalInfos", drugProfessionalInfos);
        request.setAttribute("sourceTypeOptions", drugProfessionalInfoDao.findAllSourceTypes());
        request.setAttribute("evidenceLevelOptions", drugProfessionalInfoDao.findAllEvidenceLevels());
        request.setAttribute("selectedKeyword", keyword == null ? "" : keyword);
        request.setAttribute("selectedSourceType", sourceType == null ? "" : sourceType);
        request.setAttribute("selectedEvidenceLevel", evidenceLevel == null ? "" : evidenceLevel);
        request.getRequestDispatcher("/views/drug_professional_info.jsp").forward(request, response);
    }

    public void drugFavorite(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String drugId = trimToNull(request.getParameter("drugId"));
        String action = trimToNull(request.getParameter("action"));
        if (drugId != null) {
            if ("remove".equalsIgnoreCase(action)) {
                drugFavoriteDao.removeFavorite(currentUser.getId(), drugId);
            } else {
                drugFavoriteDao.addFavorite(currentUser.getId(), drugId);
            }
        }
        response.sendRedirect(resolveReturnUrl(request));
    }

    private void markDrugFavorites(HttpServletRequest request, List<Drug> drugs) {
        Set<String> favoriteDrugIds = findFavoriteDrugIds(request);
        for (Drug drug : drugs) {
            drug.setFavorited(favoriteDrugIds.contains(drug.getId()));
        }
    }

    private void markDrugLabelFavorites(HttpServletRequest request, List<DrugLabel> drugLabels) {
        Set<String> favoriteDrugIds = findFavoriteDrugIds(request);
        for (DrugLabel drugLabel : drugLabels) {
            drugLabel.setFavorited(favoriteDrugIds.contains(drugLabel.getDrugId()));
        }
    }

    private void markDosingGuidelineFavorites(HttpServletRequest request, List<DosingGuideline> dosingGuidelines) {
        Set<String> favoriteDrugIds = findFavoriteDrugIds(request);
        for (DosingGuideline dosingGuideline : dosingGuidelines) {
            dosingGuideline.setFavorited(favoriteDrugIds.contains(dosingGuideline.getDrugId()));
        }
    }

    private void markDrugProfessionalInfoFavorites(HttpServletRequest request,
                                                   List<DrugProfessionalInfo> drugProfessionalInfos) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return;
        }
        Set<String> favoriteDrugNames = drugFavoriteDao.findFavoriteDrugNamesByUserId(currentUser.getId());
        for (DrugProfessionalInfo drugProfessionalInfo : drugProfessionalInfos) {
            drugProfessionalInfo.setFavorited(
                    favoriteDrugNames.contains(normalizeDrugName(drugProfessionalInfo.getDrugName())));
        }
    }

    private Set<String> findFavoriteDrugIds(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Collections.emptySet();
        }
        return drugFavoriteDao.findFavoriteDrugIdsByUserId(currentUser.getId());
    }

    private User getCurrentUser(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object currentUser = request.getSession(false).getAttribute("currentUser");
        return currentUser instanceof User ? (User) currentUser : null;
    }

    private String resolveReturnUrl(HttpServletRequest request) {
        String returnUrl = request.getParameter("returnUrl");
        String contextPath = request.getContextPath();
        if (isAllowedReturnUrl(returnUrl, contextPath)) {
            return returnUrl;
        }
        return contextPath + "/drugs";
    }

    private boolean isAllowedReturnUrl(String returnUrl, String contextPath) {
        if (returnUrl == null || !returnUrl.startsWith(contextPath + "/") || returnUrl.startsWith(contextPath + "//")) {
            return false;
        }
        String path = returnUrl.substring(contextPath.length());
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        return "/drugs".equals(path)
                || "/drugLabels".equals(path)
                || "/dosingGuideline".equals(path)
                || "/drugProfessionalInfo".equals(path);
    }

    private String normalizeDrugName(String drugName) {
        return drugName == null ? "" : drugName.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
