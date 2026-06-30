package org.springframework.samples.petclinic.views.owners

import htmlflow.HtmlView
import htmlflow.div
import htmlflow.dyn
import htmlflow.tbody
import htmlflow.tr
import htmlflow.view
import org.springframework.samples.petclinic.Routes
import org.springframework.samples.petclinic.owner.Owner
import org.springframework.samples.petclinic.pet.Pet
import org.springframework.samples.petclinic.pet.PetRepository
import org.springframework.samples.petclinic.views.fragments.errorDiv
import org.springframework.samples.petclinic.views.fragments.layout
import org.springframework.samples.petclinic.views.fragments.rowElementInput
import org.springframework.samples.petclinic.views.pets.addNewPetButton
import org.springframework.samples.petclinic.views.pets.petAddButtons
import org.springframework.samples.petclinic.views.pets.petEditButtons
import org.springframework.samples.petclinic.views.pets.petRowAndVisits
import org.springframework.samples.petclinic.views.pets.petsInputs
import org.springframework.samples.petclinic.views.pets.tableBodyPetsAndVisits
import org.springframework.stereotype.Component
import org.xmlet.htmlapifaster.Div
import org.xmlet.htmlapifaster.Tbody
import org.xmlet.htmlapifaster.b
import org.xmlet.htmlapifaster.br
import org.xmlet.htmlapifaster.button
import org.xmlet.htmlapifaster.div
import org.xmlet.htmlapifaster.h2
import org.xmlet.htmlapifaster.i
import org.xmlet.htmlapifaster.table
import org.xmlet.htmlapifaster.tbody
import org.xmlet.htmlapifaster.td
import org.xmlet.htmlapifaster.th
import org.xmlet.htmlapifaster.tr
import org.xmlet.htmlflow.datastar.attributes.dataAttr
import org.xmlet.htmlflow.datastar.attributes.dataIndicator
import org.xmlet.htmlflow.datastar.attributes.dataOn
import org.xmlet.htmlflow.datastar.events.Click

internal const val ERROR_MSG = "Either fields are empty or telephone number has other characters."

@Component
class OwnersDetails(
    private val pets: PetRepository,
) {
    val view: HtmlView<Any> = layout { ownerDetails() }

    val defaultOwnerTableView: HtmlView<Owner> =
        view {
            tbody {
                attrId("owner-table-body")
                ownerTableBody()
            }
        }

    val editOwnerTableView: HtmlView<Owner> =
        view {
            tbody {
                attrId("owner-table-body")
                ownerEditTable()
            }
        }

    val errorEditOwnerView: HtmlView<String> =
        view {
            div {
                dyn { errorMsg: String ->
                    errorDiv(errorMsg)
                }
            }
        }

    val defaultPetsTableView: HtmlView<Owner> =
        view {
            tbody {
                attrId("pets-table-body")
                tableBodyPetsAndVisits()
            }
        }

    val petAddView: HtmlView<Owner> =
        view {
            tr {
                attrId("pets-add")
                td {
                    addAttr("valign", "top")
                    petsInputs(pets, "-new")
                }
                td {
                    addAttr("valign", "top")
                    table {
                        attrClass("visits-table")
                        tbody {
                            petAddButtons()
                        }
                    }
                }
            }
        }

    val petEditRow: HtmlView<Pet> =
        view {
            tr {
                dyn { pet: Pet ->
                    attrId("row-pet-${pet.id}")
                    td {
                        addAttr("valign", "top")
                        petsInputs(pets, "${pet.id}")
                    }
                }
                td {
                    addAttr("valign", "top")
                    table {
                        attrClass("visits-table")
                        tbody {
                            petEditButtons(false)
                        }
                    }
                }
            }
        }

    val petRow: HtmlView<Pet> =
        view {
            tr {
                dyn { pet: Pet ->
                    attrId("row-pet-${pet.id}")
                    petRowAndVisits(pet.owner!!, pet)
                }
            }
        }

    private fun Div<*>.ownerDetails() {
        h2 { text("Owner Information") }

        table {
            attrClass("table table-striped")
            tbody {
                attrId("owner-table-body")
                ownerTableBody()
            }
        }

        div {
            attrId("owners-buttons")
            editOwnerButton()
            addNewPetButton(false)
        }

        br {}
        br {}
        br {}
        h2 { text("Pets and Visits") }
        table {
            attrClass("table table-striped")
            tbody {
                attrId("pets-table-body")
                tableBodyPetsAndVisits()
            }
        }
    }

    private fun Div<*>.editOwnerButton(disabled: Boolean = false) {
        button {
            attrId("edit-owner")
            attrDisabled(disabled)
            dyn { owner: Owner ->
                dataOn(Click) {
                    get(Routes.ownerEdit(owner.id))
                }
                attrClass("btn btn-primary")
            }
            text("Edit Owner")
        }
    }
    fun ownerButtonsView(disabled: Boolean) = view<Owner> {
        div {
            attrId("owners-buttons")
            editOwnerButton(disabled)
            addNewPetButton(disabled)
        }
    }

    private fun Tbody<*>.ownerTableBody() {
        tr {
            th { text("Name") }
            td {
                b {
                    dyn { owner: Owner ->
                        text(owner.firstName + " " + owner.lastName)
                    }
                }
            }
        }

        tr {
            th { text("Address") }
            td {
                dyn { owner: Owner ->
                    text(owner.address)
                }
            }
        }

        tr {
            th { text("City") }
            td {
                dyn { owner: Owner ->
                    text(owner.city)
                }
            }
        }
        tr {
            th { text("Telephone") }
            td {
                dyn { owner: Owner ->
                    text(owner.telephone)
                }
            }
        }
    }

    private fun Tbody<*>.ownerEditTable() {
        tr {
            th { text("First Name") }
            rowElementInput("first-name")
            th { text("Last Name") }
            rowElementInput("last-name")
        }
        tr {
            th { text("Address") }
            rowElementInput("address")
        }
        tr {
            th { text("City") }
            rowElementInput("city")
        }
        tr {
            th { text("Telephone") }
            rowElementInput("telephone")
        }
        tr {
            td {
                dyn { owner: Owner ->
                    button {
                        attrId("save-owner")
                        attrClass("btn btn-primary")
                        dataOn(Click) {
                            patch(Routes.ownerEdit(owner.id))
                        }
                        val fetching = dataIndicator("_fetching")
                        dataAttr("disabled") { +fetching }
                        i { attrClass("pixelarticons:check") }
                        text("Save")
                    }
                }

                dyn { owner: Owner ->
                    button {
                        attrId("cancel-edit")
                        attrClass("btn btn-primary")
                        dataOn(Click) {
                            get(Routes.ownerEditCancel(owner.id))
                        }
                        val fetching = dataIndicator("_fetching")
                        dataAttr("disabled") { +fetching }
                        i { attrClass("pixelarticons:close") }
                        text("Cancel")
                    }
                }
            }
        }
    }
}
