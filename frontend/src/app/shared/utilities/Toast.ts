

export abstract class Toast {
    static toast(message: string) {
        const snackBar = document.getElementById('snackbar');
        snackBar.className = 'show';
        snackBar.innerHTML = message;
        setTimeout(() => snackBar.className = snackBar.className.replace('show', ''), 3000);
    }

    static checkNonEmptyFields(...fields: HTMLDataElement[]): boolean {
        return this.checkNonEmptyStrings(...fields.map(field => field.value))
    }

    static checkNonEmptyStrings(...fields: string[]): boolean {
        let existsEmptyString = false;
        for (const field of fields){
            if (field === '') {
                existsEmptyString = true;
                break;
            }
        }
        if (existsEmptyString) {
            Toast.toast('Some data in the form is empty, try again');
        }
        return !existsEmptyString;
    }
}
