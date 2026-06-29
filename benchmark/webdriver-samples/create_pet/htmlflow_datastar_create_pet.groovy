import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100))
def petName = 'Fluffy-' + System.currentTimeMillis()
def ownerId = WDS.vars.get('OWNER_ID')
def port = WDS.vars.get('HTMLFLOW_PORT') ?: '8081'

// 1. Load the owner details page (full page load)
WDS.browser.get("http://localhost:${port}/owners/" + ownerId)
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("h2")).isDisplayed()
    } catch (e) { false }
})

// 2. Click "Add New Pet" button to trigger SSE GET that patches the form into the DOM
WDS.browser.findElement(By.xpath("//button[contains(., 'Add New Pet')]")).click()
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("#input-name-new")).isDisplayed()
    } catch (e) { false }
})

// 3. Fill form
def nameInput = WDS.browser.findElement(By.cssSelector("#input-name-new"))
nameInput.clear()
nameInput.sendKeys(petName)

def birthDateInput = WDS.browser.findElement(By.cssSelector("#input-birth-date-new"))
birthDateInput.clear()
birthDateInput.sendKeys('2020-01-15')

def typeSelect = new Select(WDS.browser.findElement(By.cssSelector("select[name='type']")))
typeSelect.selectByValue('cat')

// 4. Submit form via button click (triggers DataStar v1 data-on:click -> SSE POST)
// Wait for save button to be enabled (_fetching indicator overlay gone)
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("#save-pet:not([disabled])")).isDisplayed()
    } catch (e) { false }
})
// Use JS click to bypass headless Chrome compositing artifacts
WDS.browser.executeScript("arguments[0].click()",
    WDS.browser.findElement(By.cssSelector("#save-pet")))

// 5. Wait for the form to be removed (SSE response replaced #pets-add with pet row)
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("#pets-add")).isDisplayed()
        false
    } catch (e) { true }
})

// 6. Wait for the new pet name to appear in a <dd> (verifies pet was rendered correctly)
wait.until({ d ->
    try {
        d.findElement(By.xpath("//dd[contains(., '" + petName + "')]")).isDisplayed()
    } catch (e) { false }
})

WDS.sampleResult.setResponseData("Pet created: " + petName)
WDS.sampleResult.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT)
WDS.sampleResult.setSuccessful(true)
