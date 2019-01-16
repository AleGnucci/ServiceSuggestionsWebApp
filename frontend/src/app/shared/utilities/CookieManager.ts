export abstract class CookieManager {

    static SESSION_COOKIE_NAME = 'vertx-web.session';

    static getCookie(name: string): string {
        const cookies: Array<string> = document.cookie.split(';');
        const cookiesCount: number = cookies.length;
        const cookieName = `${name}=`;
        let c: string;

        for (let i = 0; i < cookiesCount; i += 1) {
            c = cookies[i].replace(/^\s+/g, '');
            if (c.indexOf(cookieName) === 0) {
                return c.substring(cookieName.length, c.length);
            }
        }
        return '';
    }

    static deleteCookie(name) {
        CookieManager.setCookie(name, '', -1);
    }

    static setCookie(name: string, value: string, expireDays: number, path: string = '') {
        const date = new Date();
        date.setTime(date.getTime() + expireDays * 24 * 60 * 60 * 1000);
        const expires = `expires=${date.toUTCString()}` + ';path=/';
        const cpath = path ? `; path=${path}` : '';
        document.cookie = `${name}=${value}; ${expires}${cpath}`;
    }
}