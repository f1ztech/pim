$(function() {
	$('.pim-login-button').popover( {
		html : true,
		content : $('#login-content').html()
	});
	$('#login-content').remove();
	
	$('.pim-list-item.small').click(function() {
		$(this).popover({
			html : true,
			content : $('#popover-content').html(),
			placement : 'bottom'
		}).popover('show');
	});
});

