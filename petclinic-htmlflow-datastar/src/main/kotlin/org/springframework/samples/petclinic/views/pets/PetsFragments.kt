package org.springframework.samples.petclinic.views.pets

import htmlflow.dyn
import org.springframework.samples.petclinic.Routes
import org.springframework.samples.petclinic.owner.Owner
import org.springframework.samples.petclinic.pet.Pet
import org.springframework.samples.petclinic.pet.PetRepository
import org.springframework.samples.petclinic.views.fragments.rowElementInputInline
import org.springframework.samples.petclinic.views.visits.visitsTableBody
import org.springframework.samples.petclinic.views.visits.visitsTableHead
import org.xmlet.htmlapifaster.Div
import org.xmlet.htmlapifaster.EnumTypeInputType
import org.xmlet.htmlapifaster.Tbody
import org.xmlet.htmlapifaster.Td
import org.xmlet.htmlapifaster.Tr
import org.xmlet.htmlapifaster.button
import org.xmlet.htmlapifaster.dd
import org.xmlet.htmlapifaster.dl
import org.xmlet.htmlapifaster.dt
import org.xmlet.htmlapifaster.i
import org.xmlet.htmlapifaster.option
import org.xmlet.htmlapifaster.select
import org.xmlet.htmlapifaster.table
import org.xmlet.htmlapifaster.td
import org.xmlet.htmlapifaster.tr
import org.xmlet.htmlflow.datastar.Signal
import org.xmlet.htmlflow.datastar.attributes.dataAttr
import org.xmlet.htmlflow.datastar.attributes.dataBind
import org.xmlet.htmlflow.datastar.attributes.dataIndicator
import org.xmlet.htmlflow.datastar.attributes.dataOn
import org.xmlet.htmlflow.datastar.attributes.dataSignal
import org.xmlet.htmlflow.datastar.events.Click

// Used in OwnersDetails
internal fun Div<*>.addNewPetButton(disabled: Boolean) {
    button {
        dyn { owner: Owner ->
            dataOn(Click) {
                get(Routes.petNew(owner.id))
            }
            attrClass("btn btn-primary")
            attrDisabled(disabled)
        }
        text("Add New Pet")
    }
}

internal fun Tbody<*>.tableBodyPetsAndVisits() {
    dyn { owner: Owner ->
        owner.getPets().forEach { pet ->
            tr {
                petRowAndVisits(owner, pet)
            }
        }
    }
}

internal fun Tr<*>.petRowAndVisits(
    owner: Owner,
    pet: Pet,
) {
    attrId("row-pet-${pet.id}")
    petRow(pet)
    td {
        addAttr("valign", "top")
        table {
            attrClass("visits-table")
            visitsTableHead()
            visitsTableBody(owner, pet)
        }
    }
}

internal fun Tr<*>.petRow(pet: Pet) {
    td {
        addAttr("valign", "top")
        dl {
            attrClass("dl-horizontal")
            dt { text("Name") }
            dd { text(pet.name) }
            dt { text("Birth Date") }
            dd { text(pet.birthDate) }
            dt { text("Type") }
            dd { text(pet.type) }
        }
    }
}

internal fun Td<*>.petsInputs(
    pets: PetRepository,
    signalSuffix: String,
) {
    dl {
        attrClass("dl-horizontal")
        dt { text("Name") }
        dd { rowElementInputInline("name$signalSuffix") }
        dt { text("Birth Date") }
        dd { rowElementInputInline("birth-date$signalSuffix", EnumTypeInputType.DATE) }
        dt { text("Type") }
        dd {
            select {
                attrName("type")
                dataBind("type$signalSuffix")
                pets.findPetTypes().forEach { item ->
                    option {
                        attrValue(item.name ?: "")
                        text(item.name ?: "")
                    }
                }
            }
        }
    }
}

internal fun Tbody<*>.petAddButtons() {
    tr {
        td {
            dyn { owner: Owner ->
                button {
                    attrId("save-pet")
                    attrClass("btn btn-primary")
                    dataOn(Click) {
                        post(Routes.petNew(owner.id))
                    }
                    val fetching = dataIndicator("_fetching")
                    dataAttr("disabled") { +fetching }
                    i { attrClass("pixelarticons:check") }
                    text("save")
                }
            }
            dyn { owner: Owner ->
                button {
                    attrId("cancel-pet")
                    attrClass("btn btn-primary")
                    dataOn(Click) {
                        get(Routes.petNewCancel(owner.id))
                    }
                    val fetching = dataIndicator("_fetching")
                    dataAttr("disabled") { +fetching }
                    i { attrClass("pixelarticons:check") }
                    text("cancel")
                }
            }
        }
    }
}

internal fun Tbody<*>.petEditButtons(disabled: Boolean) {
    tr {
        td {
            dyn { pet: Pet ->
                button {
                    attrId("save-edit-pet")
                    attrClass("btn btn-primary")
                    attrDisabled(disabled)
                    dataOn(Click) {
                        patch(Routes.petEdit(pet.owner!!.id, pet.id))
                    }
                    val fetching = dataIndicator("_fetching")
                    dataAttr("disabled") { +fetching }
                    i { attrClass("pixelarticons:check") }
                    text("save")
                }
            }
            dyn { pet: Pet ->
                button {
                    attrId("cancel-pet")
                    attrClass("btn btn-primary")
                    attrDisabled(disabled)
                    dataOn(Click) {
                        get(Routes.petEditCancel(pet.owner!!.id, pet.id))
                    }
                    val fetching = dataIndicator("_fetching")
                    dataAttr("disabled") { +fetching }
                    i { attrClass("pixelarticons:check") }
                    text("cancel")
                }
            }
        }
    }
}

