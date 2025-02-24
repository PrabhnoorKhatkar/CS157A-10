package artauction;

import artauction.user.Admin;
import artauction.user.User;
import artauction.user.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Servlet implementation class EditArtwork
 */
@WebServlet(name = "EditArtwork", urlPatterns = {"/App/EditArtwork"})
public class EditArtwork extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public EditArtwork() {
        super();
        // Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Auto-generated method stub
        int artworkID = Integer.parseInt(request.getParameter("id"));
        Integer userID = (Integer) request.getSession().getAttribute("userID");
        var user = (User) request.getSession().getAttribute("user");

        // Check if visting user is owner
        ArtworkPageDAO artworkPage = new ArtworkPageDAO();
        var isOwner = artworkPage.checkArtworkAccount(userID, artworkID);
        request.setAttribute("isOwner", isOwner);
        var allowedToEdit = isOwner || user instanceof Admin;
        if (!allowedToEdit) {
            request.setAttribute("message", "You are not allowed to edit this artwork"); // not used
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        ArtworkDAO searchDAO = new ArtworkDAO();
        Artwork artwork = searchDAO.getArtworkById(artworkID);
        request.setAttribute("artwork", artwork);

        List<String> tags = searchDAO.getTagsByArtworkID(artworkID);
        request.setAttribute("tags", tags);

        AuctionDAO auctionDAO = new AuctionDAO();
        Auction auction = auctionDAO.getAuctionByArtworkID(artworkID);

        User highestBidder = auctionDAO.getHighestBidder(artworkID);

        // Check if current user is highest bidder
        if (highestBidder.getDisplayName() != null) {
            request.setAttribute("isHighest", highestBidder.getId() == (user.getId()));
        }

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        // Check if auction is over and reserve not met and is ACTIVE
        // If so update to RESERVE NOT MET in DB
        if (currentTimestamp.after(auction.getEndTimestamp()) && auction.getAmount() < auction.getReserve() && auction.getResult().equals("ACTIVE")) {
            auctionDAO.reserveNotMetArtwork(artworkID);
        }
        // Check if auction is over and reserve is met
        // If so select winningUser and update to UNSOLD in DB
        else if (currentTimestamp.after(auction.getEndTimestamp()) && auction.getAmount() >= auction.getReserve() && auction.getResult().equals("ACTIVE")) {
            User winningUser = auctionDAO.getHighestBidder(artworkID);
            UserDAO userDAO = new UserDAO();
            int winningUserID = userDAO.getUserIDByDisplayName(winningUser.getDisplayName());
            auctionDAO.endArtwork(artworkID);

            request.setAttribute("winningUser", winningUserID == userID);

            highestBidder = auctionDAO.getHighestBidder(artworkID);
        }
        // Check if auction is over and reserve is met and is UNSOLD status
        // If so select winningUser and display to auction losers
        else if (currentTimestamp.after(auction.getEndTimestamp()) && auction.getAmount() >= auction.getReserve() && auction.getResult().equals("UNSOLD")) {
            User winningUser = auctionDAO.getHighestBidder(artworkID);
            UserDAO userDAO = new UserDAO();
            int winningUserID = userDAO.getUserIDByDisplayName(winningUser.getDisplayName());

            request.setAttribute("winningUser", winningUserID == userID);

            highestBidder = auctionDAO.getHighestBidder(artworkID);
        }
        auction = auctionDAO.getAuctionByArtworkID(artworkID);
        request.setAttribute("auction", auction);
        request.setAttribute("highestBidder", highestBidder);

        int ownerUserID = artworkPage.getUserIDByArtworkID(artworkID);
        request.setAttribute("ownerUserID", ownerUserID);

        // Get the owner display name using the ArtworkPageDAO
        String ownerDisplayName = artworkPage.getUserDisplayNameByUserID(ownerUserID);
        request.setAttribute("ownerDisplayName", ownerDisplayName); // Add the owner display name to the request

        SaveArtworkDAO saveArtworkDAO = new SaveArtworkDAO();
        boolean checkSave = saveArtworkDAO.checkSave(userID, artworkID);
        request.setAttribute("checkSave", checkSave);
        // Forward to the artwork details JSP page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/App/edit-artwork.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        int artworkID = Integer.parseInt(request.getParameter("artworkID"));
        //System.out.println("artworkid: " + artworkID);

        ArtworkDAO searchDAO = new ArtworkDAO();

        Artwork artwork = searchDAO.getArtworkById(artworkID);
        request.setAttribute("artwork", artwork);

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String artist = request.getParameter("artist");

        // If no changes made do nothing
        if (title.equals(artwork.getTitle()) && description.equals(artwork.getDescription()) && artist.equals(artwork.getArtist())) {
            // No changes made, forward back to the same page with a message
            request.setAttribute("message", "No changes were made to the artwork.");
        } else {
            // Process the update
            ArtworkDAO editDAO = new ArtworkDAO();
            Artwork updateArtwork = new Artwork(artworkID, title, description, artist);
            String result = editDAO.updateArtwork(updateArtwork);

            System.err.println(result);
            if (!result.equals("Successfully Updated")) {
                request.setAttribute("message", "Failed to update artwork. Please try again.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/App/ArtworkPage?id=" + artworkID);
    }

}
