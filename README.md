module-ogone
============

This is a Core9 module for an Ogone connection.
It makes use of the widgets flow, in which the Ogone data handler handles the ogone specific connections.
The Core9 Commerce module can use this module as a payment gateway.

To make proper use of this module, create a PaymentMethod and refer to a newly created widget.
The Payment-Ogone DataHandler has all the required config fields available.
Use a proper delegate template for Ogone, like

    {namespace core9.commerce.payment}

    /**
     * @param paymentData
     */
    {deltemplate core9.commerce.payment.type variant="'ogonetest'"}
    {if isNonnull($paymentData.status)}
      <p class="alert alert-danger">{$paymentData.status}</p>
    {/if}
    <p class="lead">You chose Ogone Payement, proceed by clicking below.</p>
    <form method="post" action="{$paymentData.link}" id="form1" name="form1">
      <!-- parameters -->
      {foreach $param in keys($paymentData.ogoneconfig)}
      <input type="hidden" name="{$param}" value="{$paymentData.ogoneconfig[$param]}" />
      {/foreach}
      <button class="btn btn-success" type="submit" id="submit2" name="submit2">
        Proceed to Ogone
      </button>
    </form>
    {/deltemplate}



You should also add a return url pagemodel which has the Payment-Ogone-Feedback handler. Set it up with the widget bundle ID of the order finished page.