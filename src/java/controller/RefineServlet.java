
package controller;

import dal.CategoryDAO;
import dal.ProductDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import model.Product;

@WebServlet(name = "RefineServlet", urlPatterns = {"/refine"})
public class RefineServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CategoryDAO d = new CategoryDAO();
        ProductDAO p = new ProductDAO();
        List<Category> categories = d.getAll();
        List<Product> allproduct = p.getAll();
        List<Product> productsCid = p.getAll();
        Boolean[] chid = new Boolean[categories.size() + 1];
        String cid_refine_raw = request.getParameter("cid_refine");
        String priceFrom_raw = request.getParameter("pricefrom");
        String priceTo_raw = request.getParameter("priceto");
     
        String nameSearch = request.getParameter("nameSearch");
      
        String stringForLink = "";
        int cid_refine = 0;

        //RefineBrand
        String[] cid_refinee_raw = request.getParameterValues("cid_refinee");
        int[] cid_refinee = null;

        //RefineHeaderBrand
        if (cid_refine_raw != null) {
            cid_refine = Integer.parseInt(cid_refine_raw);
            productsCid = p.getProductsByCategoryid(cid_refine);
            if (cid_refine == 0) {
                chid[0] = true;
            }
        }
        //RefinePrice
        double price1 = ((priceFrom_raw == null || "".equals(priceFrom_raw)) ? 0 : Double.parseDouble(priceFrom_raw));
        double price2 = ((priceTo_raw == null || "".equals(priceTo_raw)) ? 0 : Double.parseDouble(priceTo_raw));

        //RefineHeaderBrand
        if (cid_refine_raw == null) {
            chid[0] = true;
        } else {
            chid[0] = false;
        }

        //RefineBrand
        if (cid_refinee_raw != null) {
            cid_refinee = new int[cid_refinee_raw.length];
            for (int i = 0; i < cid_refinee.length; i++) {
                cid_refinee[i] = Integer.parseInt(cid_refinee_raw[i]);
            }
            productsCid = p.searchByCheckBox(cid_refinee);
        }

        if (price1 != 0 || price2 != 0) {
            productsCid = p.searchByPrice(price1, price2, cid_refinee);
        }

        //Paging
        int page = 1, numPerPage = 12;
        int size = productsCid.size();
        int numberpage = ((size % numPerPage == 0) ? (size / 12) : (size / 12) + 1);
        String xpage = request.getParameter("page");
        if (xpage == null) {
            page = 1;
        } else {
            page = Integer.parseInt(xpage);
        }
        int start, end;
        start = (page - 1) * 12;

        end = Math.min(page * numPerPage, size);
        List<Product> listByPage = p.getListByPage(productsCid, start, end);

        //RefineBrand
        if ((cid_refinee_raw != null) && (cid_refinee[0] != 0)) {
            chid[0] = false;
            for (int i = 1; i < chid.length; i++) {
                if (isCheck(categories.get(i - 1).getId(), cid_refinee)) {
                    stringForLink += "cid_refinee=" + i + "&";
                    chid[i] = true;
                } else {
                    chid[i] = false;
                }
            }
        }
        if (stringForLink.endsWith("&")) {
            stringForLink = stringForLink.substring(0, stringForLink.length() - 1);
        } else if (stringForLink.isEmpty()) {
            stringForLink = "cid_refinee=0";
        }

       
        
        request.setAttribute("stringForLink", stringForLink);

        if (price1 != 0 || price2 != 0) {
            request.setAttribute("price1", price1);
            request.setAttribute("price2", price2);
        }

        if (nameSearch != null) {
            listByPage = p.searchByName(nameSearch);
        }

        Category ca = d.getCategoryById(cid_refine);

        request.setAttribute("dis25", p.getNumberProductsByDiscount(0.25));
        request.setAttribute("dis50", p.getNumberProductsByDiscount(0.5));
        request.setAttribute("dis75", p.getNumberProductsByDiscount(0.75));
        request.setAttribute("searchAtHome", nameSearch);
        request.setAttribute("cat", ca);
        request.setAttribute("category", categories);
        request.setAttribute("productPage", listByPage);
        request.setAttribute("chid", chid);
        request.setAttribute("cid_refinee", cid_refinee);
        request.setAttribute("cid_refine", cid_refine);
        request.setAttribute("page", page);
        request.setAttribute("allproduct", allproduct);
        request.setAttribute("numberpage", numberpage);
        request.getRequestDispatcher("refine.jsp").forward(request, response);
    }

   

   

    //RefineBrand
    private boolean isCheck(int d, int[] id) {
        if (id == null) {
            return false;
        } else {
            for (int i = 0; i < id.length; i++) {
                if (id[i] == d) {
                    return true;
                }
            }
        }
        return false;
    }

}
